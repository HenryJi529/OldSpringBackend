package com.morningstar.old.system.properties;


import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "app.jwt")
@Data
public class JwtProperties {
    // 应用密钥
    private String secretKey;
    // 过期时间
    private Long ttl;
}
