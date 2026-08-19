package com.morningstar.old.infra.config;

import com.morningstar.old.infra.constant.RedisConstant;
import org.noear.solon.ai.chat.ChatModel;
import org.noear.solon.ai.chat.ChatRole;
import org.noear.solon.ai.chat.ChatSession;
import org.noear.solon.ai.chat.ChatSessionFactory;
import org.noear.solon.ai.chat.message.ChatMessage;
import org.noear.solon.ai.mcp.McpChannel;
import org.noear.solon.ai.mcp.client.McpClientProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * AI 基础设施 Bean 装配（与具体业务无关，各业务模块注入使用）：
 * <ul>
 *     <li>{@link McpClientProvider}：MCP 客户端，连接本应用内嵌的 MCP server；</li>
 *     <li>{@link ChatModel}：模型客户端（OpenAI 兼容），业务侧按需绑定 MCP 工具做自动调用；</li>
 *     <li>{@link ChatSessionFactory}：Redis 会话工厂（按账号隔离，纯 Redis 读写，无进程内缓存）。</li>
 * </ul>
 */
@Configuration
public class AiConfig {

    /**
     * MCP 客户端（懒连接，首个请求时才完成 initialize 握手）
     */
    @Bean(destroyMethod = "close")
    public McpClientProvider mcpClientProvider(@Value("${app.ai.mcp.url:http://localhost:8088/mcp/mcp-demo}") String url) {
        return McpClientProvider.builder()
                .channel(McpChannel.STREAMABLE)
                .url(url)
                .build();
    }

    /**
     * 聊天模型（不在构建期绑工具：MCP 工具在首个请求时才拉取，启动期连接自己还没就绪会 Connection refused）
     */
    @Bean
    public ChatModel chatModel(@Value("${app.ai.chat.base-url}") String baseUrl,
                               @Value("${app.ai.chat.api-key:}") String apiKey,
                               @Value("${app.ai.chat.model}") String model) {
        return ChatModel.of(baseUrl)
                .apiKey(apiKey)
                .model(model)
                .build();
    }

    /**
     * 会话工厂：按账号（token 的 subject）隔离上下文，天然做到数据权限隔离。
     * 每次请求都新建轻量会话对象、直连 Redis 读写，进程内不缓存任何会话状态，
     * 单实例/多实例/重启上下文一致（Redis 是唯一真相）。
     */
    @Bean
    public ChatSessionFactory chatSessionFactory(StringRedisTemplate stringRedisTemplate) {
        return account -> new RedisChatSession(account, stringRedisTemplate);
    }

    /**
     * Redis 会话历史存储（纯 Redis 读写，无进程内缓存）。
     *
     * <p>结构：每会话一个 List，key 形如 {@code ai:chat:session:<accountId>}，
     * 每条消息以 {@link ChatMessage#toJson} 的 JSON RPUSH 入队；读取时 LRANGE 加载。
     * System 消息不落 Redis（每次请求由业务现场组装）。</p>
     */
    static class RedisChatSession implements ChatSession {

        /**
         * Redis 中保留的最大消息条数
         */
        public static final int MAX_MESSAGES = 5;

        /**
         * 会话过期时间（每次写入续期）
         */
        public static final Duration TTL = Duration.ofDays(7);

        private final String accountId;
        private final StringRedisTemplate redisTemplate;

        RedisChatSession(String accountId, StringRedisTemplate redisTemplate) {
            this.accountId = accountId;
            this.redisTemplate = redisTemplate;
        }

        private String key() {
            return RedisConstant.AI_CHAT_SESSION + RedisConstant.KEY_SEPARATOR + accountId;
        }

        @Override
        public String getSessionId() {
            return accountId;
        }

        @Override
        public List<ChatMessage> getMessages() {
            return loadMessages(0, -1);
        }

        @Override
        public List<ChatMessage> getLatestMessages(int windowSize) {
            if (windowSize <= 0) {
                return new ArrayList<>();
            }
            // 从右往左数最近 windowSize 条
            return loadMessages(-windowSize, -1);
        }

        /**
         * 从 Redis 加载指定索引范围的聊天消息（LRANGE）
         */
        private List<ChatMessage> loadMessages(long start, long end) {
            List<String> rawList = redisTemplate.opsForList().range(key(), start, end);
            if (rawList == null || rawList.isEmpty()) {
                return new ArrayList<>();
            }
            return rawList.stream().map(ChatMessage::fromJson).collect(Collectors.toList());
        }

        @Override
        public void removeLatestMessage(int windowSize) {
            if (windowSize <= 0) {
                return;
            }
            // 从最右端（最新）逐个弹出；弹出最后一个元素时 Redis 会自动删除整个 key
            for (int i = 0; i < windowSize; i++) {
                redisTemplate.opsForList().rightPop(key());
            }
            // 若仍有剩余，续期
            Long size = redisTemplate.opsForList().size(key());
            if (size != null && size > 0) {
                redisTemplate.expire(key(), TTL);
            }
        }

        @Override
        public void addMessage(Collection<? extends ChatMessage> messages) {
            if (messages == null || messages.isEmpty()) {
                return;
            }

            // System 消息不落盘（每次请求由业务现场组装）
            List<String> jsonList = messages.stream()
                    .filter(m -> m.getRole() != ChatRole.SYSTEM)
                    .map(ChatMessage::toJson)
                    .collect(Collectors.toList());
            if (jsonList.isEmpty()) {
                return;
            }

            // 追加到列表尾部
            redisTemplate.opsForList().rightPushAll(key(), jsonList);
            // 限长：只留最近 MAX_MESSAGES 条
            redisTemplate.opsForList().trim(key(), -MAX_MESSAGES, -1);
            // 续期
            redisTemplate.expire(key(), TTL);
        }

        @Override
        public boolean isEmpty() {
            Long size = redisTemplate.opsForList().size(key());
            return size == null || size == 0;
        }

        @Override
        public void clear() {
            redisTemplate.delete(key());
        }

        @Override
        public Map<String, Object> attrs() {
            // 临时属性，不持久化（当前业务未使用）
            return new HashMap<>();
        }
    }
}
