package com.morningstar.old.demo.controller;

import com.morningstar.old.infra.response.R;
import com.morningstar.old.system.util.AuthUtil;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.noear.solon.ai.chat.ChatModel;
import org.noear.solon.ai.chat.ChatResponse;
import org.noear.solon.ai.chat.ChatSession;
import org.noear.solon.ai.chat.ChatSessionFactory;
import org.noear.solon.ai.mcp.client.McpClientProvider;
import org.noear.solon.ai.chat.message.ChatMessage;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import java.util.ArrayList;
import java.util.List;

/**
 * AI 对话接口：自然语言查询 / 更新数据，基于 ChatModel + MCP client。
 *
 * <p>认证由 {@code JwtAuthenticationFilter} 完成（本接口不在白名单，自动受保护），
 * 账号从 SecurityContext 取，token 从认证 header 取并注入 MCP 工具。</p>
 */
@Tag(name = "AI示例相关接口定义")
@RestController
@RequestMapping("/demo/ai")
@RequiredArgsConstructor
@Slf4j
public class AiChatDemoController {

    private final ChatModel chatModel;
    private final ChatSessionFactory chatSessionFactory;
    private final McpClientProvider mcpClientProvider;

    /**
     * 系统提示词：只定角色和行为规则，不枚举工具（工具清单由 MCP 动态发现，模型自行探索）
     */
    private static final String SYSTEM_PROMPT =
            "你是数据助手，帮用户查询和更新 ta 自己的数据。" +
                    "规则：1) 如果工具参数需要 token 之类的凭证，系统已自动注入，无需、也不要向用户索要；" +
                    "2) 只能操作用户自己的数据；3) 用简洁的中文回答，直接引用工具返回的结果。";

    /**
     * 自然语言对话入口
     */
    @PostMapping("/chat")
    public R<String> chat(@Valid @RequestBody AiChatRequestVo req, HttpServletRequest request) {
        // 认证已由过滤器完成，账号来自 SecurityContext
        String account = AuthUtil.getUserId();
        // 从认证 header 取原始 token，注入 MCP 工具（viewData/modifyData 需要 token 解析账号）
        String token = request.getHeader(HttpHeaders.AUTHORIZATION);

        ChatSession session = chatSessionFactory.getSession(account);

        // 组装消息：系统提示词 + 历史上下文 + 本轮用户消息
        List<ChatMessage> messages = new ArrayList<>();
        messages.add(ChatMessage.ofSystem(SYSTEM_PROMPT));
        messages.addAll(session.getMessages());
        messages.add(ChatMessage.ofUser(req.getMessage()));

        ChatResponse resp;
        try {
            resp = chatModel.prompt(messages)
                    // 工具在请求期绑定（此时应用已就绪，MCP client 可连上内嵌 server 拉取工具列表）
                    .options(o -> o.toolAdd(mcpClientProvider)
                            .toolContextPut("token", token))
                    .call();
        } catch (Exception e) {
            log.error("AI 调用失败, account={}", account, e);
            return R.error("AI 调用失败: " + e.getMessage());
        }

        if (resp.getError() != null) {
            log.error("AI 返回错误, account={}: {}", account, resp.getError().getMessage());
            return R.error("AI 返回错误: " + resp.getError().getMessage());
        }

        String answer = resp.getResultContent();
        if (answer == null || answer.isEmpty()) {
            answer = "（模型没有返回文本内容）";
        }

        // 回写会话（只保留用户消息 + 最终回答，中间的 tool_call 过程不入历史）
        session.addMessage(ChatMessage.ofUser(req.getMessage()));
        session.addMessage(ChatMessage.ofAssistant(answer));

        return R.ok(answer);
    }

    @Data
    @Schema(description = "AI 对话请求")
    public static class AiChatRequestVo {
        @Schema(description = "用户自然语言消息")
        @NotBlank(message = "message 不能为空")
        private String message;
    }
}
