package com.wisdomhub.util;

import com.wisdomhub.config.JwtProperties;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

/**
 * JWT 工具类（生成/解析标准 JWT Token）
 */
@Component
public class JwtUtil {
    
    private static final Logger log = LoggerFactory.getLogger(JwtUtil.class);
    
    private final SecretKey secretKey;
    private final Long expireSeconds;
    
    public JwtUtil(JwtProperties jwtProperties) {
        // 基于配置的 secret 生成签名密钥（HMAC-SHA256）
        this.secretKey = Keys.hmacShaKeyFor(
            jwtProperties.getSecret().getBytes(StandardCharsets.UTF_8)
        );
        this.expireSeconds = jwtProperties.getExpireSeconds();
    }
    
    /**
     * 生成 JWT Token
     * @param userId 用户ID
     * @param email 用户邮箱
     * @return JWT Token（eyJ 开头）
     */
    public String generateToken(Long userId, String email) {
        Date now = new Date();
        Date expireDate = new Date(now.getTime() + expireSeconds * 1000);
        
        return Jwts.builder()
                .setSubject(email)                          // 主题（用户邮箱）
                .claim("userId", userId)                    // 自定义声明：用户ID
                .claim("email", email)                      // 自定义声明：用户邮箱
                .setIssuedAt(now)                           // 签发时间
                .setExpiration(expireDate)                  // 过期时间
                .signWith(secretKey, SignatureAlgorithm.HS256) // 签名算法
                .compact();
    }
    
    /**
     * 解析 JWT Token
     * @param token JWT 字符串
     * @return Claims（包含 userId、email 等信息）
     * @throws io.jsonwebtoken.JwtException Token 非法/过期
     */
    public Claims parseToken(String token) {
        try {
            return Jwts.parserBuilder()
                    .setSigningKey(secretKey)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
        } catch (ExpiredJwtException e) {
            log.warn("JWT 已过期: {}", e.getMessage());
            throw new com.wisdomhub.exception.UnauthorizedException("Token 已过期，请重新登录");
        } catch (MalformedJwtException e) {
            log.warn("JWT 格式错误: {}", e.getMessage());
            throw new com.wisdomhub.exception.UnauthorizedException("Token 格式错误");
        } catch (JwtException e) {
            log.warn("JWT 非法: {}", e.getMessage());
            throw new com.wisdomhub.exception.UnauthorizedException("Token 非法");
        }
    }
    
    /**
     * 从 Claims 中获取用户ID
     */
    public Long getUserIdFromClaims(Claims claims) {
        Object userId = claims.get("userId");
        if (userId instanceof Integer) {
            return ((Integer) userId).longValue();
        }
        return (Long) userId;
    }
    
    /**
     * 从 Claims 中获取用户邮箱
     */
    public String getEmailFromClaims(Claims claims) {
        return claims.get("email", String.class);
    }
    
    /**
     * 验证 Token 是否有效（不抛异常版本）
     */
    public boolean validateToken(String token) {
        try {
            parseToken(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}