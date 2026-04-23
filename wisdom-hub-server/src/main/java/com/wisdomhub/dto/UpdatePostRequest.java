package com.wisdomhub.dto;

import jakarta.validation.constraints.NotNull;

/**
 * 修改帖子请求 DTO
 */
public class UpdatePostRequest {
    
    /**
     * 帖子 ID（必填）
     */
    @NotNull(message = "帖子 ID 不能为空")
    private Long id;
    
    /**
     * 标题（可选）
     */
    private String title;
    
    /**
     * 内容（可选）
     */
    private String content;
    
    /**
     * 封面图（可选）
     */
    private String coverImage;
    
    /**
     * 视频链接（可选）
     */
    private String videoUrl;
    
    /**
     * 可见性（可选）：0-公开，1-私密
     */
    private Integer visibility;
    
    public UpdatePostRequest() {
    }
    
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
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
    
    public String getCoverImage() {
        return coverImage;
    }
    
    public void setCoverImage(String coverImage) {
        this.coverImage = coverImage;
    }
    
    public String getVideoUrl() {
        return videoUrl;
    }
    
    public void setVideoUrl(String videoUrl) {
        this.videoUrl = videoUrl;
    }
    
    public Integer getVisibility() {
        return visibility;
    }
    
    public void setVisibility(Integer visibility) {
        this.visibility = visibility;
    }
    
    @Override
    public String toString() {
        return "UpdatePostRequest{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", content='" + content + '\'' +
                ", coverImage='" + coverImage + '\'' +
                ", videoUrl='" + videoUrl + '\'' +
                ", visibility=" + visibility +
                '}';
    }
}