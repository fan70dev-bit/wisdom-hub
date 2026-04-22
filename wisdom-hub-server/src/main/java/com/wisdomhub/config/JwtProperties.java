package com.wisdomhub.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * JWT 配置属性
 */
@Component
@ConfigurationProperties(prefix = "app.jwt")
public class JwtProperties {
    
    /**
     * JWT 签名密钥（至少 32 字节）
     */
    private String secret;
    
    /**
     * Token 有效期（秒）
     */
    private Long expireSeconds = 7200L;
    
    public JwtProperties() {
    }
    
    public String getSecret() {
        return secret;
    }
    
    public void setSecret(String secret) {
        this.secret = secret;
    }
    
    public Long getExpireSeconds() {
        return expireSeconds;
    }
    
    public void setExpireSeconds(Long expireSeconds) {
        this.expireSeconds = expireSeconds;
    }
    
    @Override
    public String toString() {
        return "JwtProperties{" +
                "secret='[PROTECTED]'" +
                ", expireSeconds=" + expireSeconds +
                '}';
    }
}