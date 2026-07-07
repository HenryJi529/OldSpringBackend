package com.morningstar.old.infra.util.desensitize;

import java.util.regex.Pattern;

public enum SensitiveStrategy {
    // 手机号：隐藏中间四位
    PHONE("(\\d{3})\\d{4}(\\d{4})", "$1****$2"),
    // 身份证号：隐藏中间部分
    ID_CARD("(\\d{4})\\d{10}(\\w{4})", "$1****$2"),
    // 邮箱：保留前两位和域名部分
    EMAIL("(\\w{1,2})[^@]*(@.*)", "$1****$2");

    private final Pattern pattern;
    private final String replacement;

    SensitiveStrategy(String regex, String replacement) {
        this.pattern = Pattern.compile(regex);
        this.replacement = replacement;
    }

    public String apply(String s) {
        if (s == null || s.isEmpty()) return s;
        // 复用预编译好的 pattern，只创建轻量级的 matcher
        return pattern.matcher(s).replaceAll(replacement);
    }
}
