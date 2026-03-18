package com.example.demo.util;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Component
@Slf4j
public class JwtUtil {

    @Value("${jwt.secret:mySecretKey}")
    private String secret;

    // 设置为30天（单位：毫秒）
    @Value("${jwt.expiration:2592000000}")  // 30天 = 30 * 24 * 60 * 60 * 1000
    private Long expiration = 2592000000L;  // 默认30天

    // 确保密钥长度为32字节
    private SecretKey getSigningKey() {
        byte[] keyBytes = secret.getBytes(StandardCharsets.UTF_8);
        byte[] paddedKey = new byte[32];

        // 复制并循环填充
        for (int i = 0; i < 32; i++) {
            paddedKey[i] = keyBytes[i % keyBytes.length];
        }

        return Keys.hmacShaKeyFor(paddedKey);
    }

    // 生成token - 0.12.x版本
    public String generateToken(String userId, String username) {
        Instant now = Instant.now();
        Instant expirationTime = now.plus(expiration, ChronoUnit.MILLIS);

        return Jwts.builder()
                .claim("userId", userId)
                .claim("username", username)
                .issuedAt(Date.from(now))
                .expiration(Date.from(expirationTime))
                .signWith(getSigningKey(), Jwts.SIG.HS256)
                .compact();
    }

    // 从token中获取用户ID
    public String getUserIdFromToken(String token) {
        Jws<Claims> jws = parseToken(token);
        return jws.getPayload().get("userId", String.class);
    }

    // 从token中获取用户名
    public String getUsernameFromToken(String token) {
        Jws<Claims> jws = parseToken(token);
        return jws.getPayload().get("username", String.class);
    }

    // 验证token是否有效
    public boolean validateToken(String token) {
        try {
            Jws<Claims> jws = parseToken(token);
            Date expiration = jws.getPayload().getExpiration();
            return expiration.after(new Date());
        } catch (JwtException | IllegalArgumentException e) {
            log.warn("JWT Token验证失败: {}", e.getMessage());
            return false;
        }
    }

    // 解析token
    private Jws<Claims> parseToken(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token);
    }
}