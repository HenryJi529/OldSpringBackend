## ADDED Requirements

### Requirement: 每用户静态数据存储
系统 SHALL 以一个 static 变量（内存 Map）作为数据源，以用户账号（account）为键，为每个账号存储一份独立的数据。

#### Scenario: 数据按账号隔离
- **WHEN** 以账号 100000 写入数据 "A"，以账号 100001 写入数据 "B"
- **THEN** 账号 100000 读取到 "A"，账号 100001 读取到 "B"，互不影响

#### Scenario: 预置数据
- **WHEN** 应用启动
- **THEN** 数据源中已存在 `application.yml` 中配置的账号（100000/100001/100002）各自的初始数据

### Requirement: 通过调用方 token 鉴定用户身份
系统 SHALL 接收调用方 token 作为接口参数，使用 `JwtUtil` 验签（含过期校验）并解析出 subject 作为当前用户账号；解析失败时拒绝访问。

#### Scenario: 有效 token 解析出身份
- **WHEN** 传入签名有效且未过期的 token，其 subject 为 100000
- **THEN** 系统判定当前用户为账号 100000

#### Scenario: 无效 token 被拒绝
- **WHEN** 传入签名无效或已过期的 token
- **THEN** 系统返回错误，且不返回、不修改任何数据

### Requirement: 查看数据（按身份确定范围）
系统 SHALL 提供"查看数据"能力，返回当前账号自己的那份数据，不暴露其他账号的数据。

#### Scenario: 查看本人数据
- **WHEN** 账号 100000 的调用方 token 调用"查看数据"
- **THEN** 返回账号 100000 自己的数据，不包含其他账号的数据

### Requirement: 修改数据（按身份确定范围）
系统 SHALL 提供"修改数据"能力，仅修改当前账号自己的那份数据。

#### Scenario: 修改本人数据
- **WHEN** 账号 100000 的调用方 token 调用"修改数据"并传入新值
- **THEN** 仅账号 100000 的数据被更新，其他账号的数据保持不变

### Requirement: 通过 MCP 暴露查看/修改工具
系统 SHALL 通过 solon-ai 将"查看数据""修改数据"暴露为 MCP 工具，采用 STREAMABLE 通道，端点为 `/mcp/data/sse`。

#### Scenario: MCP 客户端发现并调用工具
- **WHEN** MCP 客户端连接 `http://localhost:8088/mcp/data/sse` 并列出工具
- **THEN** 可见"查看数据""修改数据"两个工具，且传入有效 token 调用成功
