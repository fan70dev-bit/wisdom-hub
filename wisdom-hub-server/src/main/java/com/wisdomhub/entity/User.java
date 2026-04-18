package com.wisdomhub.entity;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * 用户实体类
 */
public class User {

    private Long id;
    private String email;
    private String nickname;
    private Integer status;
    private LocalDateTime createTime;
    private LocalDateTime lastLoginTime;
    private String lastLoginIp;

    // 无参构造
    public User() {
    }

    // 全参构造
    public User(Long id, String email, String nickname, Integer status,
                LocalDateTime createTime, LocalDateTime lastLoginTime, String lastLoginIp) {
        this.id = id;
        this.email = email;
        this.nickname = nickname;
        this.status = status;
        this.createTime = createTime;
        this.lastLoginTime = lastLoginTime;
        this.lastLoginIp = lastLoginIp;
    }

    // Builder 模式（手动实现）
    public static UserBuilder builder() {
        return new UserBuilder();
    }

    // Getter 方法
    public Long getId() {
        return id;
    }

    public String getEmail() {
        return email;
    }

    public String getNickname() {
        return nickname;
    }

    public Integer getStatus() {
        return status;
    }

    public LocalDateTime getCreateTime() {
        return createTime;
    }

    public LocalDateTime getLastLoginTime() {
        return lastLoginTime;
    }

    public String getLastLoginIp() {
        return lastLoginIp;
    }

    // Setter 方法
    public void setId(Long id) {
        this.id = id;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public void setCreateTime(LocalDateTime createTime) {
        this.createTime = createTime;
    }

    public void setLastLoginTime(LocalDateTime lastLoginTime) {
        this.lastLoginTime = lastLoginTime;
    }

    public void setLastLoginIp(String lastLoginIp) {
        this.lastLoginIp = lastLoginIp;
    }

    @Override
    public String toString() {
        return "User{" +
                "id=" + id +
                ", email='" + email + '\'' +
                ", nickname='" + nickname + '\'' +
                ", status=" + status +
                ", createTime=" + createTime +
                ", lastLoginTime=" + lastLoginTime +
                ", lastLoginIp='" + lastLoginIp + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        User user = (User) o;
        return Objects.equals(id, user.id) && Objects.equals(email, user.email);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, email);
    }

    /**
     * Builder 内部类
     */
    public static class UserBuilder {
        private Long id;
        private String email;
        private String nickname;
        private Integer status;
        private LocalDateTime createTime;
        private LocalDateTime lastLoginTime;
        private String lastLoginIp;

        UserBuilder() {
        }

        public UserBuilder id(Long id) {
            this.id = id;
            return this;
        }

        public UserBuilder email(String email) {
            this.email = email;
            return this;
        }

        public UserBuilder nickname(String nickname) {
            this.nickname = nickname;
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

        public User build() {
            return new User(id, email, nickname, status, createTime, lastLoginTime, lastLoginIp);
        }
    }
}