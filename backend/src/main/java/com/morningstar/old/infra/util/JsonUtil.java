package com.morningstar.old.infra.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateDeserializer;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateSerializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import com.morningstar.old.infra.util.desensitize.SensitiveSerializerModifier;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;


public class JsonUtil {
    private static SimpleModule getLongModule() {
        SimpleModule module = new SimpleModule();
        module.addSerializer(Long.class, new ToStringSerializer());
        module.addSerializer(Long.TYPE, new ToStringSerializer());
        return module;
    }

    private static SimpleModule getLocalDateTimeModule() {
        SimpleModule module = new SimpleModule();
        DateTimeFormatter formatter = DateTimeFormatter.ISO_DATE_TIME;
        module.addSerializer(LocalDateTime.class, new LocalDateTimeSerializer(formatter));
        module.addDeserializer(LocalDateTime.class, new LocalDateTimeDeserializer(formatter));
        return module;
    }

    private static SimpleModule getLocalDateModule() {
        SimpleModule module = new SimpleModule();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        module.addSerializer(LocalDate.class, new LocalDateSerializer(formatter));
        module.addDeserializer(LocalDate.class, new LocalDateDeserializer(formatter));
        return module;
    }

    private static SimpleModule getSensitiveModule() {
        SimpleModule module = new SimpleModule();
        module.setSerializerModifier(new SensitiveSerializerModifier());
        return module;
    }

    public static ObjectMapper objectMapper() {
        ObjectMapper mapper = new ObjectMapper();
//        mapper.setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.ANY); // 不推荐，存在安全风险, @JsonIgnore失效

        // 补充类型
        mapper.findAndRegisterModules();
        // 设置Long的序列化
        mapper.registerModule(getLongModule());
        // 设置LocalDateTime的序列化与反序列化
        mapper.registerModule(getLocalDateTimeModule());
        // 设置LocalDate的序列化与反序列化
        mapper.registerModule(getLocalDateModule());

        // 设置脱敏模块
        mapper.registerModule(getSensitiveModule());

        return mapper;
    }
}

