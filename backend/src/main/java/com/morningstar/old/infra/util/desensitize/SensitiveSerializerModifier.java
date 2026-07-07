package com.morningstar.old.infra.util.desensitize;

import com.fasterxml.jackson.databind.BeanDescription;
import com.fasterxml.jackson.databind.SerializationConfig;
import com.fasterxml.jackson.databind.ser.BeanPropertyWriter;
import com.fasterxml.jackson.databind.ser.BeanSerializerModifier;

import java.util.List;

public class SensitiveSerializerModifier extends BeanSerializerModifier {

    @Override
    public List<BeanPropertyWriter> changeProperties(SerializationConfig config,
                                                     BeanDescription beanDesc,
                                                     List<BeanPropertyWriter> beanProperties) {
        for (BeanPropertyWriter writer : beanProperties) {
            Sensitive sensitive = writer.getAnnotation(Sensitive.class);
            if (sensitive != null && writer.getType().isTypeOrSubTypeOf(String.class)) {
                // 如果有注解且是 String 类型，则替换其序列化器
                writer.assignSerializer(new SensitiveSerializer(sensitive.strategy()));
            }
        }
        return beanProperties;
    }
}