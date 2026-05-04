package com.wisdomhub.dto;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * 登录响应（扩展版）
 */
public class LoginResponse {
    
    private Long userId;
    private String accountId;      // 新增
    private String email;
    private String username;       // 修改为 username
    private String avatarUrl;      // 新增
    private String token;
    private Boolean isNewUser;
    private LocalDateTime loginTime;
    
    public LoginResponse() {
    }
    
    public LoginResponse(Long userId, String accountId, String email, String username,
                        String avatarUrl, String token, Boolean isNewUser, LocalDateTime loginTime) {
        this.userId = userId;
        this.accountId = accountId;
        this.email = email;
        this.username = username;
        this.avatarUrl = avatarUrl;
        this.token = token;
        this.isNewUser = isNewUser;
        this.loginTime = loginTime;
    }
    
    public static LoginResponseBuilder builder() {
        return new LoginResponseBuilder();
    }
    
    // Getter/Setter
    public Long getUserId() {
        return userId;
    }
    
    public void setUserId(Long userId) {
        this.userId = userId;
    }
    
    public String getAccountId() {
        return accountId;
    }
    
    public void setAccountId(String accountId) {
        this.accountId = accountId;
    }
    
    public String getEmail() {
        return email;
    }
    
    public void setEmail(String email) {
        this.email = email;
    }
    
    public String getUsername() {
        return username;
    }
    
    public void setUsername(String username) {
        this.username = username;
    }
    
    public String getAvatarUrl() {
        return avatarUrl;
    }
    
    public void setAvatarUrl(String avatarUrl) {
        this.avatarUrl = avatarUrl;
    }
    
    public String getToken() {
        return token;
    }
    
    public void setToken(String token) {
        this.token = token;
    }
    
    public Boolean getIsNewUser() {
        return isNewUser;
    }
    
    public void setIsNewUser(Boolean isNewUser) {
        this.isNewUser = isNewUser;
    }
    
    public LocalDateTime getLoginTime() {
        return loginTime;
    }
    
    public void setLoginTime(LocalDateTime loginTime) {
        this.loginTime = loginTime;
    }
    
    @Override
    public String toString() {
        return "LoginResponse{" +
                "userId=" + userId +
                ", accountId='" + accountId + '\'' +
                ", email='" + email + '\'' +
                ", username='" + username + '\'' +
                ", avatarUrl='" + avatarUrl + '\'' +
                ", token='" + token + '\'' +
                ", isNewUser=" + isNewUser +
                ", loginTime=" + loginTime +
                '}';
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        LoginResponse that = (LoginResponse) o;
        return Objects.equals(userId, that.userId) && Objects.equals(token, that.token);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(userId, token);
    }
    
    /**
     * Builder 内部类
     */
    public static class LoginResponseBuilder {
        private Long userId;
        private String accountId;
        private String email;
        private String username;
        private String avatarUrl;
        private String token;
        private Boolean isNewUser;
        private LocalDateTime loginTime;
        
        LoginResponseBuilder() {
        }
        
        public LoginResponseBuilder userId(Long userId) {
            this.userId = userId;
            return this;
        }
        
        public LoginResponseBuilder accountId(String accountId) {
            this.accountId = accountId;
            return this;
        }
        
        public LoginResponseBuilder email(String email) {
            this.email = email;
            return this;
        }
        
        public LoginResponseBuilder username(String username) {
            this.username = username;
            return this;
        }
        
        public LoginResponseBuilder avatarUrl(String avatarUrl) {
            this.avatarUrl = avatarUrl;
            return this;
        }
        
        public LoginResponseBuilder token(String token) {
            this.token = token;
            return this;
        }
        
        public LoginResponseBuilder isNewUser(Boolean isNewUser) {
            this.isNewUser = isNewUser;
            return this;
        }
        
        public LoginResponseBuilder loginTime(LocalDateTime loginTime) {
            this.loginTime = loginTime;
            return this;
        }
        
        public LoginResponse build() {
            return new LoginResponse(userId, accountId, email, username, avatarUrl,
                    token, isNewUser, loginTime);
        }
    }
}