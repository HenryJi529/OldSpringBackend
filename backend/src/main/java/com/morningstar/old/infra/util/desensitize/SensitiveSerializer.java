package com.morningstar.old.infra.util.desensitize;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

import java.io.IOException;

public class SensitiveSerializer extends JsonSerializer<Object> {
    private final SensitiveStrategy strategy;

    public SensitiveSerializer(SensitiveStrategy strategy) {
        this.strategy = strategy;
    }

    @Override
    public void serialize(Object value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
        /* 类型预检 */
        if (!(value instanceof String)) {
            if (value == null) {
                gen.writeNull();
            } else {
                // 这种找法比 gen.writeObject 安全，不会死循环
                serializers.findValueSerializer(value.getClass()).serialize(value, gen, serializers);
            }
            return;
        }
        String str = (String) value;

        /* 空串处理 */
        if (str.isEmpty()) {
            gen.writeString("");
            return;
        }

        /* 脱敏逻辑 */
        try {
            gen.writeString(strategy.apply(str));
        } catch (Exception e) {
            // 兜底：万一脱敏失败，原样输出，保证业务不中断
            gen.writeString(str);
        }
    }
}