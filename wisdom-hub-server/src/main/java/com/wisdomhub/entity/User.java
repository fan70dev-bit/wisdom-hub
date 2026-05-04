package com.wisdomhub.entity;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * 用户实体类（重构版）
 */
public class User {
    
    private Long id;
    
    /**
     * 账号 ID（8-10位随机数字，注册时生成）
     */
    private String accountId;
    
    /**
     * 邮箱（登录凭证，唯一）
     */
    private String email;
    
    /**
     * 显示名称（注册时默认为 "Wisdom用户_随机后缀"）
     */
    private String username;
    
    /**
     * 昵称（兼容旧版，可废弃）
     */
    private String nickname;
    
    /**
     * 头像地址（默认阿里云 OSS 初始头像）
     */
    private String avatarUrl;
    
    /**
     * 状态：0-正常，1-临时封禁，2-已注销
     */
    private Integer status;
    
    /**
     * 创建时间
     */
    private LocalDateTime createTime;
    
    /**
     * 最后登录时间
     */
    private LocalDateTime lastLoginTime;
    
    /**
     * 最后登录IP
     */
    private String lastLoginIp;
    
    /**
     * 上次修改资料时间（用于12小时冷却检查）
     */
    private LocalDateTime lastProfileUpdate;
    
    // 无参构造
    public User() {
    }
    
    // 全参构造
    public User(Long id, String accountId, String email, String username, String nickname,
                String avatarUrl, Integer status, LocalDateTime createTime,
                LocalDateTime lastLoginTime, String lastLoginIp, LocalDateTime lastProfileUpdate) {
        this.id = id;
        this.accountId = accountId;
        this.email = email;
        this.username = username;
        this.nickname = nickname;
        this.avatarUrl = avatarUrl;
        this.status = status;
        this.createTime = createTime;
        this.lastLoginTime = lastLoginTime;
        this.lastLoginIp = lastLoginIp;
        this.lastProfileUpdate = lastProfileUpdate;
    }
    
    // Builder 模式
    public static UserBuilder builder() {
        return new UserBuilder();
    }
    
    // Getter/Setter
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
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
    
    public String getNickname() {
        return nickname;
    }
    
    public void setNickname(String nickname) {
        this.nickname = nickname;
    }
    
    public String getAvatarUrl() {
        return avatarUrl;
    }
    
    public void setAvatarUrl(String avatarUrl) {
        this.avatarUrl = avatarUrl;
    }
    
    public Integer getStatus() {
        return status;
    }
    
    public void setStatus(Integer status) {
        this.status = status;
    }
    
    public LocalDateTime getCreateTime() {
        return createTime;
    }
    
    public void setCreateTime(LocalDateTime createTime) {
        this.createTime = createTime;
    }
    
    public LocalDateTime getLastLoginTime() {
        return lastLoginTime;
    }
    
    public void setLastLoginTime(LocalDateTime lastLoginTime) {
        this.lastLoginTime = lastLoginTime;
    }
    
    public String getLastLoginIp() {
        return lastLoginIp;
    }
    
    public void setLastLoginIp(String lastLoginIp) {
        this.lastLoginIp = lastLoginIp;
    }
    
    public LocalDateTime getLastProfileUpdate() {
        return lastProfileUpdate;
    }
    
    public void setLastProfileUpdate(LocalDateTime lastProfileUpdate) {
        this.lastProfileUpdate = lastProfileUpdate;
    }
    
    @Override
    public String toString() {
        return "User{" +
                "id=" + id +
                ", accountId='" + accountId + '\'' +
                ", email='" + email + '\'' +
                ", username='" + username + '\'' +
                ", avatarUrl='" + avatarUrl + '\'' +
                ", status=" + status +
                ", createTime=" + createTime +
                ", lastLoginTime=" + lastLoginTime +
                ", lastProfileUpdate=" + lastProfileUpdate +
                '}';
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        User user = (User) o;
        return Objects.equals(id, user.id) && Objects.equals(accountId, user.accountId);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(id, accountId);
    }
    
    /**
     * Builder 内部类
     */
    public static class UserBuilder {
        private Long id;
        private String accountId;
        private String email;
        private String username;
        private String nickname;
        private String avatarUrl;
        private Integer status;
        private LocalDateTime createTime;
        private LocalDateTime lastLoginTime;
        private String lastLoginIp;
        private LocalDateTime lastProfileUpdate;
        
        UserBuilder() {
        }
        
        public UserBuilder id(Long id) {
            this.id = id;
            return this;
        }
        
        public UserBuilder accountId(String accountId) {
            this.accountId = accountId;
            return this;
        }
        
        public UserBuilder email(String email) {
            this.email = email;
            return this;
        }
        
        public UserBuilder username(String username) {
            this.username = username;
            return this;
        }
        
        public UserBuilder nickname(String nickname) {
            this.nickname = nickname;
            return this;
        }
        
        public UserBuilder avatarUrl(String avatarUrl) {
            this.avatarUrl = avatarUrl;
            return this;
        }
        
        public UserBuilder status(Integer status) {
            this.status = status;
            return this;
        }
        
        public UserBuilder createTime(LocalDateTime createTime) {
            this.createTime = createTime;
            return this;
        }
        
        public UserBuilder lastLoginTime(LocalDateTime lastLoginTime) {
            this.lastLoginTime = lastLoginTime;
            return this;
        }
        
        public UserBuilder lastLoginIp(String lastLoginIp) {
            this.lastLoginIp = lastLoginIp;
            return this;
        }
        
        public UserBuilder lastProfileUpdate(LocalDateTime lastProfileUpdate) {
            this.lastProfileUpdate = lastProfileUpdate;
            return this;
        }
        
        public User build() {
            return new User(id, accountId, email, username, nickname, avatarUrl, status,
                    createTime, lastLoginTime, lastLoginIp, lastProfileUpdate);
        }
    }
}