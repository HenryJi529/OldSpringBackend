## 1. 依赖与构建配置

- [ ] 1.1 在 `backend/pom.xml` 增加 solon 依赖：`solon-lib`、`solon-web-servlet`、`solon-ai`、`solon-ai-mcp`
- [ ] 1.2 在 `backend/pom.xml` 增加 `solon-parent`（4.0.6）的 `dependencyManagement` import
- [ ] 1.3 给 `maven-compiler-plugin` 增加 `-parameters` 编译参数（并保持 source/target 为 1.8）
- [ ] 1.4 `mvn -q -DskipTests compile` 确认依赖可解析、编译通过

## 2. 静态数据源与身份解析

- [ ] 2.1 新建 `mcp/AppDataStore.java`：`static Map<String,String>`，按 account 隔离，预置 100000/100001/100002 初始数据，提供 `get(account)` / `set(account, value)`
- [ ] 2.2 实现身份解析：用 `JwtUtil.parse(token)` 取 subject 得账号，解析失败抛错（复用现有异常/响应体系）

## 3. 查看/修改接口（web2mcp 共用）

- [ ] 3.1 新建 `mcp/DataController.java`：`@RestController` + `@McpServerEndpoint` + `implements IMcpServerEndpoint`
- [ ] 3.2 实现 `viewData(token)`：`@RequestMapping` + `@ToolMapping` 共用，返回该账号自己的数据
- [ ] 3.3 实现 `modifyData(token, value)`：同上，仅修改该账号自己的数据

## 4. MCP 集成

- [ ] 4.1 新建 `mcp/McpServerConfig.java`：`@PostConstruct` 内 `Solon.start(...)`（`enableScanning(false)`）、`springCom2Endpoint()` 收集 `IMcpServerEndpoint` bean、注册 `SolonServletFilter` 到 `/mcp/*`；`@PreDestroy` 停止 Solon
- [ ] 4.2 确认/处理 `--cfg=mcpserver.yml`（建空文件或移除该 flag）
- [ ] 4.3 在 `SecurityConfig` 白名单 `API_WHITE_LIST_ALL_METHOD` 加入 `/mcp/**`

## 5. 验证

- [ ] 5.1 启动应用，确认日志无报错、内嵌 Solon 正常拉起
- [ ] 5.2 curl 验证 REST：`GET /data?token=<有效token>` 与 `POST /data?token=...&value=...` 返回正确、跨账号隔离生效、无效 token 报错
- [ ] 5.3 用参考示例的 MCP 客户端方式（或 curl 探测 `/mcp/data/sse`）确认工具可被发现
- [ ] 5.4 提醒用户在 Claude Code 配置该 MCP server（`claude mcp add --transport sse old-backend http://localhost:8088/mcp/data/sse`）并试调 `viewData` / `modifyData`
