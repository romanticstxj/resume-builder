package com.resume.config;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Component
public class JwtUtil {

    @Value("${jwt.secret:resume-builder-secret-key-must-be-at-least-256-bits-long-for-hs256}")
    private String secret;

    @Value("${jwt.expiration:3600000}")       // access token 默认 1h
    private long expirationMs;

    @Value("${jwt.refresh-expiration:604800000}") // refresh token 默认 7天
    private long refreshExpirationMs;

    private SecretKey getSigningKey() {
        byte[] keyBytes = secret.getBytes(StandardCharsets.UTF_8);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    public String generateToken(String subject) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + expirationMs);
        return Jwts.builder()
                .setSubject(subject)
                .setIssuedAt(now)
                .setExpiration(expiry)
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    /** 统一入口：用数据库 id 生成 access token，subject = "user:{id}"，有效期短 */
    public String generateTokenForUser(Long userId) {
        return generateToken("user:" + userId);
    }

    /** 生成 refresh token：随机 UUID，不含业务信息，有效期长 */
    public String generateRefreshToken() {
        return java.util.UUID.randomUUID().toString().replace("-", "");
    }

    public long getRefreshTokenExpirationMs() {
        return refreshExpirationMs;
    }

    /** 从 token 解析 userId，兼容旧格式（email subject） */
    public Long getUserIdFromToken(String token) {
        String subject = getUsernameFromToken(token);
        if (subject.startsWith("user:")) {
            return Long.parseLong(subject.substring(5));
        }
        return null; // 旧格式 email subject，返回 null 由调用方降级处理
    }

    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder()
                    .setSigningKey(getSigningKey())
                    .build()
                    .parseClaimsJws(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    public String getUsernameFromToken(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody()
                .getSubject();
    }
}
