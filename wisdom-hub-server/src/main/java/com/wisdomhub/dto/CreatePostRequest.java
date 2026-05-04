package com.wisdomhub.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * 创建帖子请求（去封面图版本）
 */
public class CreatePostRequest {
    
    private String title;
    
    @NotBlank(message = "内容不能为空")
    private String content;
    
    private String videoUrl;
    
    private Integer type;
    
    private Boolean isPrivate;
    
    public CreatePostRequest() {
    }
    
    public String getTitle() {
        return title;
    }
    
    public void setTitle(String title) {
        this.title = title;
    }
    
    public String getContent() {
        return content;
    }
    
    public void setContent(String content) {
        this.content = content;
    }
    
    public String getVideoUrl() {
        return videoUrl;
    }
    
    public void setVideoUrl(String videoUrl) {
        this.videoUrl = videoUrl;
    }
    
    public Integer getType() {
        return type;
    }
    
    public void setType(Integer type) {
        this.type = type;
    }
    
    public Boolean getIsPrivate() {
        return isPrivate;
    }
    
    public void setIsPrivate(Boolean isPrivate) {
        this.isPrivate = isPrivate;
    }
    
    @Override
    public String toString() {
        return "CreatePostRequest{" +
                "title='" + title + '\'' +
                ", content='" + content + '\'' +
                ", videoUrl='" + videoUrl + '\'' +
                ", type=" + type +
                ", isPrivate=" + isPrivate +
                '}';
    }
}