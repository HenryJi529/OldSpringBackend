CREATE TABLE `sql_execute_history` (
    `order` INT NOT NULL AUTO_INCREMENT COMMENT "执行顺序",
    `name` VARCHAR(100) NOT NULL COMMENT "脚本名称",
    `version` VARCHAR(50) DEFAULT NULL COMMENT "版本号，形如: 1.0、2.1",
    `description` VARCHAR(200) NULL COMMENT "功能描述",
    `executed_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT "执行完成时间",
    `status` TINYINT NOT NULL COMMENT "执行状态，可选值: 1【成功】、0【失败】",
    PRIMARY KEY (`order`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;