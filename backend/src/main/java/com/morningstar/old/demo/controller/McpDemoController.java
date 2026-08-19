package com.morningstar.old.demo.controller;

import com.morningstar.old.infra.exception.BaseException;
import com.morningstar.old.infra.response.ResponseCode;
import com.morningstar.old.system.util.JwtUtil;
import io.jsonwebtoken.Claims;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.noear.solon.ai.annotation.ToolMapping;
import org.noear.solon.ai.mcp.McpChannel;
import org.noear.solon.ai.mcp.server.IMcpServerEndpoint;
import org.noear.solon.ai.mcp.server.annotation.McpServerEndpoint;
import org.noear.solon.annotation.Param;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * webapi 与 mcp-server 共用代码（控制器上，分别加上各自的注解）
 */
@McpServerEndpoint(channel = McpChannel.STREAMABLE, name = "mcp-demo", mcpEndpoint = "/mcp/mcp-demo")
@RequestMapping("/demo/mcp")
@Tag(name = "MCP示例相关接口定义")
@RestController
@RequiredArgsConstructor
public class McpDemoController implements IMcpServerEndpoint {

    private final JwtUtil jwtUtil;

    /**
     * 查看数据：解析调用方 token → 账号 → 返回该账号自己的数据
     */
    @ToolMapping(description = "查看数据（传入调用方 token，返回该账号自己的数据）")
    @GetMapping
    public String viewData(@Param(description = "调用方 token") @RequestParam("token") String token) {
        String account = resolveAccount(token);
        String data = AppDataStore.get(account);
        return data == null ? "账号 " + account + " 暂无数据" : data;
    }

    /**
     * 修改数据：解析调用方 token → 账号 → 仅修改该账号自己的数据
     */
    @ToolMapping(description = "修改数据（传入调用方 token 与新数据，仅修改该账号自己的数据）")
    @PostMapping
    public String modifyData(@Param(description = "调用方 token") @RequestParam("token") String token,
                             @Param(description = "新数据") @RequestParam("value") String value) {
        return AppDataStore.set(resolveAccount(token), value);
    }

    /**
     * 用已有 JwtUtil 验签（含过期校验）并解析出账号；解析失败抛业务异常
     */
    private String resolveAccount(String token) {
        if (token == null) {
            throw new BaseException(ResponseCode.TOKEN_INVALID);
        }
        if (!token.startsWith("Bearer ")) {
            token = "Bearer " + token;
        }
        Claims claims = jwtUtil.parse(token);
        if (claims == null) {
            throw new BaseException(ResponseCode.TOKEN_INVALID);
        }
        return claims.getSubject();
    }

    /**
     * 以 static 变量为数据源的内存数据存储（按账号隔离一份数据）
     * 每个账号只有一份数据，模拟"数据权限"：调用方只能查看/修改自己的那份
     */
    public static class AppDataStore {
        /**
         * key = 账号（account），value = 该账号自己的数据
         */
        private static final Map<String, String> DATA = new ConcurrentHashMap<>();

        static {
            DATA.put("100000", "王二的初始数据");
            DATA.put("100001", "张三的初始数据");
            DATA.put("100002", "李四的初始数据");
        }

        public static String get(String account) {
            return DATA.get(account);
        }

        public static String set(String account, String value) {
            DATA.put(account, value);
            return value;
        }
    }

}
