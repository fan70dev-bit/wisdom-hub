package com.wisdomhub.dto;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * 登录响应
 */
public class LoginResponse {

    private Long userId;
    private String email;
    private String nickname;
    private String token;
    private Boolean isNewUser;
    private LocalDateTime loginTime;

    public LoginResponse() {
    }

    public LoginResponse(Long userId, String email, String nickname,
                         String token, Boolean isNewUser, LocalDateTime loginTime) {
        this.userId = userId;
        this.email = email;
        this.nickname = nickname;
        this.token = token;
        this.isNewUser = isNewUser;
        this.loginTime = loginTime;
    }

    public static LoginResponseBuilder builder() {
        return new LoginResponseBuilder();
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
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
                ", email='" + email + '\'' +
                ", nickname='" + nickname + '\'' +
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
        private String email;
        private String nickname;
        private String token;
        private Boolean isNewUser;
        private LocalDateTime loginTime;

        LoginResponseBuilder() {
        }

        public LoginResponseBuilder userId(Long userId) {
            this.userId = userId;
            return this;
        }

        public LoginResponseBuilder email(String email) {
            this.email = email;
            return this;
        }

        public LoginResponseBuilder nickname(String nickname) {
            this.nickname = nickname;
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
            return new LoginResponse(userId, email, nickname, token, isNewUser, loginTime);
        }
    }
}