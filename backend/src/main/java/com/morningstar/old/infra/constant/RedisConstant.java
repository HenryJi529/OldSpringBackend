package com.morningstar.old.infra.constant;

public class RedisConstant {
    /**
     * 前缀后的分隔符
     */
    public static final String KEY_SEPARATOR = ":";

    /**
     * 认证相关前缀
     */
    public static final String AUTH_LOGIN = "auth:login"; // UserDetails

    /**
     * AI 会话相关前缀
     */
    public static final String AI_CHAT_SESSION = "ai:chat:session"; // ChatMessage 列表
}
