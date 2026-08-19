## Context

当前项目是 Spring Boot 2.3.12 + Java 8 的脚手架，已有 Spring Security（JWT 登录 + Redis 会话 + `@PreAuthorize` 方法级授权）。参考实现 `solon-ai-in-springboot2` 展示了如何把 solon 4.0.6 嵌入 Spring Boot，通过 `SolonServletFilter` 暴露 MCP 服务。已确认 solon 4.0.6 为 Java 8 字节码（class major 52），与本项目 Java 8 目标兼容，且相关依赖已在本地 `~/.m2` 就绪。

本次目标是验证「solon-ai MCP + Claude Code」链路，并模拟"MCP 平台用调用方 token 查数据、按身份确定数据范围"的形态。**传输层不做鉴权**，身份在接口层由调用方传入的 token 解析而来。

## Goals / Non-Goals

**Goals:**
- 用 static 变量做数据源，按账号隔离一份数据。
- 提供"查看数据 / 修改数据"两个能力，接收 token 参数、解析账号、按账号限定数据范围。
- 通过 solon-ai 以 MCP（STREAMABLE）暴露上述两个工具，供 Claude Code 调用。
- 沿用 `web2mcp` 模式，同一方法同时可被 REST 与 MCP 调用（REST 用于 curl 本地验证）。

**Non-Goals:**
- 传输层认证/授权（MCP 端点匿名可达，`/mcp/**` 白名单）。
- 方法级 `@PreAuthorize` 对 MCP 路径的拦截（见风险：其不生效，改由方法内身份解析保证范围）。
- 角色级数据权限（管理员看全部等），本阶段只做"每个账号看/改自己的数据"。
- 真实鉴权演进（服务 token / API Key），仅记录为后续方向。

## Decisions

1. **数据模型：`static Map<String, String>`，key = account**
   每个账号一份字符串数据，预置 `application.yml` 中的账号。选择单值而非结构化 Map，是为了让"数据权限"演示足够直白、工具签名最简（token + 一个 value）。后续可无痛扩展为 `Map<String, Map<String,Object>>`。
   - 备选：单个 static 字段——但无法体现"按用户隔离"，弃用。

2. **身份来源：token 作为工具参数，方法内解析**
   `viewData(token)` / `modifyData(token, value)`，用 `JwtUtil.parse(token)`（验签 + 过期校验）取 `subject` 得账号。不做 Redis 会话校验——因为本场景只关心"这个 token 是谁签发的"，不关心"是否已登出"；这与 `JwtAuthenticationFilter` 的职责刻意分开。
   - 备选：从 `Authorization` 头读取——但 Claude Code 侧配 header 麻烦，且用户明确"MCP 平台本身不需要 token"，弃用。

3. **接口形态：`web2mcp` 共用方法**
   `DataController` 同时标注 `@RestController` + `@McpServerEndpoint` + 方法上 `@RequestMapping` 与 `@ToolMapping` 并用，实现 `IMcpServerEndpoint` 以便被 `McpServerConfig` 自动收集。
   - 备选：REST 与 MCP 拆成两个方法——增加维护面，且偏离参考示例的推荐写法，弃用。

4. **solon 版本 4.0.6**（与参考示例一致，Java 8 兼容），端点 `name="data"`、`mcpEndpoint="/mcp/data/sse"`、`channel=STREAMABLE`。

5. **`-parameters` 编译参数**：solon 的 `@Param(description=...)` 依赖 Java 参数名解析工具参数名，必须开启，否则参数名退化为 `arg0`。

6. **`SecurityConfig` 白名单加入 `/mcp/**`**：`springSecurityFilterChain` 先于 `SolonServletFilter` 执行，不白名单会被 `.anyRequest().authenticated()` 拦成 401。

## Risks / Trade-offs

- [Spring Security 先于 Solon 拦截 `/mcp/*`] → 白名单 `/mcp/**` 放行（本阶段无鉴权，可接受）。
- [`@PreAuthorize` 对 MCP 路径不生效] → `MethodToolProvider` 反射直接调用方法，绕开 Spring AOP 代理；因此数据范围不靠注解、靠方法内 `JwtUtil` 解析账号后自行限定，两条路径行为一致。
- [token 作为参数可被重放/冒用（在有效期与验签范围内）] → 模拟场景可接受；验签能阻止伪造，但不能阻止持有效 token 的冒用，属已知取舍。
- [参考示例 `Solon.start(... "--cfg=mcpserver.yml")` 引用的配置文件仓库中并不存在] → 实现时建一个空 `mcpserver.yml` 或去掉该 flag，待验证。
- [Claude Code 的 MCP 传输类型（`sse` vs streamable-http）] → 测试时确认，配置 URL 均为 `http://localhost:8088/mcp/data/sse`。

## Migration Plan

无存量数据迁移。上线即重启应用（内嵌 Solon 随 Spring Boot 启动/关闭）。回滚：移除 `mcp` 包与 pom 依赖、撤销 `SecurityConfig` 白名单即可，无外部副作用。

## Open Questions

- Claude Code 侧 MCP 配置的确切 `type`（`sse` 或 streamable-http）与 token 传参方式，待联通时确认。
- 后续是否需要把"数据权限"从"仅本人"扩展为角色级（如管理员可查全部），依赖 `AuthProperties` 中的角色数据。
