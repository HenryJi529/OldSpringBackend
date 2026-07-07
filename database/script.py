from dataclasses import dataclass
from pathlib import Path
import re
import os

from dotenv import load_dotenv
import pymysql
from pymysql.connections import Connection
from pymysql.cursors import DictCursor
from pymysql.constants import CLIENT


BASE_DIR = Path(__file__).resolve().parent
ENV_FILE = BASE_DIR / ".env"
MIGRATION_DIR = BASE_DIR / "migration"
SCRIPT_PATTERN = re.compile(r"^V(?P<version>\d+\.\d+)_(?P<description>\w*)\.sql$")
load_dotenv(dotenv_path=ENV_FILE, verbose=True)


class DatabaseEnv:
    HOST = os.getenv("DB_HOST")
    PORT = os.getenv("DB_PORT")
    NAME = os.getenv("DB_NAME")
    USERNAME = os.getenv("DB_USERNAME")
    PASSWORD = os.getenv("DB_PASSWORD")


@dataclass(frozen=True)
class MigrationScript:
    path: Path
    version: str
    description: str

    @property
    def sort_key(self):
        version_key = tuple(int(part) for part in self.version.split("."))
        return version_key, self.description


def get_database_connection():
    return pymysql.connect(
        host=DatabaseEnv.HOST,
        port=int(DatabaseEnv.PORT),
        user=DatabaseEnv.USERNAME,
        password=DatabaseEnv.PASSWORD,
        database=DatabaseEnv.NAME,
        charset="utf8mb4",
        autocommit=False,
        client_flag=CLIENT.MULTI_STATEMENTS,
        cursorclass=pymysql.cursors.DictCursor
    )


def get_all_scripts() -> list[MigrationScript]:
    valid_scripts = []

    for file_path in MIGRATION_DIR.glob("*.sql"):
        match = SCRIPT_PATTERN.match(file_path.name)
        
        if not match:
            print(f"⚠️ 忽略命名非法的文件: {file_path.name}")
            continue

        script = MigrationScript(
            path=file_path,
            **match.groupdict()
        )
        valid_scripts.append(script)

    valid_scripts.sort(key=lambda s: s.sort_key)
    return valid_scripts


def get_failed_scripts(cursor: DictCursor) -> list[dict]:
    cursor.execute(
        """
        SELECT `name`, `version`, `description`
        FROM `sql_execute_history`
        WHERE `status` = 0
        ORDER BY `order`
        """
    )
    return list(map(lambda item: MigrationScript(path=MIGRATION_DIR / item['name'], version=item['version'], description=item['description']), cursor.fetchall()))


def print_failed_scripts(scripts: list[MigrationScript]) -> None:
    print("检测到 sql_execute_history 中存在失败记录，本次不执行任何 SQL。")
    print("请人工修复脚本与 sql_execute_history 数据库记录后再运行。")
    for script in scripts:
        print(f"路径: {script.path}; 版本: {script.version}; 描述: {script.description}")


def get_last_executed_script(cursor: DictCursor) -> MigrationScript | None:
    cursor.execute(
        """
        SELECT `name`, `version`, `description`
        FROM `sql_execute_history`
        ORDER BY `order` DESC
        LIMIT 1
        """
    )
    record = cursor.fetchone()
    if record:
        return MigrationScript(path=MIGRATION_DIR / record['name'], version=record['version'], description=record['description'])


def get_pending_scripts(cursor: DictCursor) -> list[MigrationScript]:
    all_scripts = get_all_scripts()
    last_executed_script = get_last_executed_script(cursor)
    if last_executed_script is None:
        return all_scripts

    for index, script in enumerate(all_scripts):
        if script.path.name == last_executed_script.path.name:
            return all_scripts[index + 1 :]
    raise Exception(f"最后执行记录对应脚本不存在: {last_executed_script.path.name}")


def insert_execute_history(cursor, script: MigrationScript, status: int) -> None:
    cursor.execute(
        """
        INSERT INTO `sql_execute_history` (`name`, `version`, `description`, `status`)
        VALUES (%s, %s, %s, %s)
        """,
        (script.path.name, script.version, script.description, status),
    )


def execute_script(cursor: DictCursor, connection: Connection, script: MigrationScript):
    try:
        cursor.execute(script.path.read_text(encoding="utf-8"))
        while cursor.nextset():
            pass
        insert_execute_history(cursor, script, 1)
        connection.commit()
    except Exception as ex:
        connection.rollback()
        print(f"{script.path.name}执行失败，错误信息: {ex}")
        insert_execute_history(cursor, script, 0)
        connection.commit()



def run():
    with get_database_connection() as connection:
        with connection.cursor() as cursor:
            failed_scripts = get_failed_scripts(cursor)
            if failed_scripts:
                print_failed_scripts(failed_scripts)
            pending_scripts = get_pending_scripts(cursor)
            print(f"待执行脚本数量: {len(pending_scripts)}...")
            for script in pending_scripts:
                print(f"执行脚本: {script.path.name}...")
                execute_script(cursor, connection, script)
            connection.commit()


if __name__ == "__main__":
    run()
