package com.morningstar.old.system.util;

import com.morningstar.old.system.properties.JwtProperties;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtParser;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.util.Base64;
import java.util.Date;
import java.util.Objects;

@Component
@RequiredArgsConstructor
public class JwtUtil {
    // Token前缀
    public final String TOKEN_PREFIX = "Bearer ";
    // 签名算法
    public final SignatureAlgorithm algorithm = SignatureAlgorithm.HS256;
    // 声明字段名
    public final String NAME_CLAIM = "name";
    private final JwtProperties jwtProperties;

    private SecretKey getSecretKey() {
        return new SecretKeySpec(Base64.getDecoder().decode(jwtProperties.getSecretKey()), algorithm.getJcaName());
    }

    /**
     * 生成Token
     */
    public String create(String account, String name, Long ttl) {
        if (ttl == null) {
            ttl = jwtProperties.getTtl();
        }

        return TOKEN_PREFIX + Jwts
                .builder()
                .setSubject(account)
                .claim(NAME_CLAIM, name)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + ttl))
                .signWith(getSecretKey(), algorithm)
                .compact();
    }

    public String create(String account, String name) {
        return create(account, name, null);
    }

    /**
     * 获取Token解析器
     */
    private JwtParser getParser() {
        return Jwts.parserBuilder().setSigningKey(getSecretKey()).build();
    }

    /**
     * 解析Token，如果非法返回null(包含超时检测)
     */
    public Claims parse(String token) {
        if (!token.startsWith(TOKEN_PREFIX)) {
            return null;
        }
        try {
            String content = token.substring(TOKEN_PREFIX.length());
            return getParser().parseClaimsJws(content).getBody();
        } catch (RuntimeException e) {
            return null;
        }
    }

    /**
     * 从Token中获取姓名
     */
    public String getName(String token) {
        return Objects.requireNonNull(parse(token)).get(NAME_CLAIM).toString();
    }
}
