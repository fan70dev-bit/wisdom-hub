package com.wisdomhub.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * 更新用户资料请求
 */
public class UpdateProfileRequest {
    
    /**
     * 显示名称（2-50字符）
     */
    @NotBlank(message = "用户名不能为空")
    @Size(min = 2, max = 50, message = "用户名长度必须在2-50之间")
    private String username;
    
    /**
     * 头像地址（URL）
     */
    private String avatarUrl;
    
    public UpdateProfileRequest() {
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
    
    @Override
    public String toString() {
        return "UpdateProfileRequest{" +
                "username='" + username + '\'' +
                ", avatarUrl='" + avatarUrl + '\'' +
                '}';
    }
}