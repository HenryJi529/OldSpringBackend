## Why

当前项目是 Spring Boot 2.3.12 的后端脚手架，尚无 MCP 能力。我们希望以最小成本验证「solon-ai 嵌入 Spring Boot + 通过 MCP 暴露业务接口 + Claude Code 连接调用」这条链路，并模拟"MCP 平台用调用方 token 去查数据、按身份确定数据范围"这一真实协作形态，为后续把业务能力以 MCP 方式开放给 AI 探路。

## What Changes

- 新增以 **static 变量**为数据源的内存数据存储，按用户账号（account）隔离一份数据。
- 新增"查看数据 / 修改数据"两个能力：接收调用方 token 作为参数，用已有 `JwtUtil` 验签并解析出账号，从而确定数据范围（每个账号只能查看/修改自己的那份数据）。
- 沿用 `solon-ai-in-springboot2` 的 `web2mcp` 模式：同一方法同时挂 REST 注解与 `@ToolMapping`，一份代码两种暴露方式。
- 引入 solon 4.0.6（`solon-lib` / `solon-web-servlet` / `solon-ai` / `solon-ai-mcp`），通过 `SolonServletFilter` 将 `/mcp/*` 路由进 Solon，注册为 STREAMABLE 通道的 MCP 服务端点（`/mcp/data/sse`）。
- 给 `maven-compiler-plugin` 增加 `-parameters` 编译参数（solon 依赖 Java 参数名解析工具参数名）。
- 在 `SecurityConfig` 白名单中加入 `/mcp/**`，使 MCP 端点匿名可达。
- **传输层不做认证/授权**：身份鉴定在接口层（工具方法内）完成，依据是调用方传入的 token。

## Capabilities

### New Capabilities
- `static-data-mcp`: 以 static 变量为数据源，通过调用方 token 鉴定身份并按账号隔离数据，提供查看/修改数据的接口，并通过 solon-ai 以 MCP 方式暴露。

### Modified Capabilities
<!-- 无既有 capability 的需求变更 -->

## Impact

- **依赖**：`backend/pom.xml` 新增 solon 系列依赖 + `solon-parent` 的 `dependencyManagement`，新增 `-parameters` 编译参数。
- **代码**：新增 `mcp` 包（`AppDataStore` 静态数据源 / `DataController` 查看与修改接口 / `McpServerConfig` solon 嵌入配置）。
- **配置**：`SecurityConfig.java` 白名单新增 `/mcp/**`。
- **运行**：应用启动时在 `@PostConstruct` 内额外拉起一个内嵌 Solon 实例（与 Spring Boot 共享 Servlet 容器），暴露 `http://localhost:8088/mcp/data/sse`。
