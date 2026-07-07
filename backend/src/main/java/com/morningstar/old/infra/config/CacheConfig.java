package com.morningstar.old.infra.config;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.morningstar.old.infra.util.JsonUtil;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Configuration
public class CacheConfig {

    private GenericJackson2JsonRedisSerializer getRedisSerializer() {
        ObjectMapper mapper = JsonUtil.objectMapper();
        // 支持类型存储
        mapper.activateDefaultTyping(
                mapper.getPolymorphicTypeValidator(), // 会检查类路径是否合法
                ObjectMapper.DefaultTyping.NON_FINAL,
                JsonTypeInfo.As.PROPERTY
        );

        return new GenericJackson2JsonRedisSerializer(mapper);
    }

    /**
     * 配置redisTemplate bean，并自定义数据的序列化的方式
     *
     * @param redisConnectionFactory 连接redis的工厂，底层有场景依赖启动时，自动加载
     * @return RedisTemplate<String, Object>
     */
    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory redisConnectionFactory) {
        /* 构建RedisTemplate模板对象 */
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(redisConnectionFactory);

        /* 为不同的数据结构设置不同的序列化方案 */
        // 设置key序列化方式
        template.setKeySerializer(new StringRedisSerializer());
        // 设置value序列化方式
        template.setValueSerializer(getRedisSerializer());
        // 设置hash中field字段序列化方式
        template.setHashKeySerializer(new StringRedisSerializer());
        // 设置hash中value的序列化方式
        template.setHashValueSerializer(getRedisSerializer());

        /* 初始化参数设置 */
        template.afterPropertiesSet();
        return template;
    }

}
