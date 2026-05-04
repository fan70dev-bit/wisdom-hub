package com.wisdomhub.dto;

import java.time.LocalDateTime;

/**
 * 帖子视图对象（包含作者信息）
 */
public class PostVO {
    
    // ========== 帖子基本信息 ==========
    private Long id;
    private String title;
    private String content;
    private String videoUrl;
    private Integer type;
    private Integer status;
    private String authorId;
    private Integer likeCount;
    private Integer favoriteCount;
    private Integer viewCount;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    
    // ========== 作者信息（来自 tb_user） ==========
    /**
     * 作者昵称
     */
    private String authorName;
    
    /**
     * 作者头像
     */
    private String authorAvatar;
    
    // 无参构造
    public PostVO() {
    }
    
    // Getter/Setter
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
    
    public Integer getStatus() {
        return status;
    }
    
    public void setStatus(Integer status) {
        this.status = status;
    }
    
    public String getAuthorId() {
        return authorId;
    }
    
    public void setAuthorId(String authorId) {
        this.authorId = authorId;
    }
    
    public Integer getLikeCount() {
        return likeCount;
    }
    
    public void setLikeCount(Integer likeCount) {
        this.likeCount = likeCount;
    }
    
    public Integer getFavoriteCount() {
        return favoriteCount;
    }
    
    public void setFavoriteCount(Integer favoriteCount) {
        this.favoriteCount = favoriteCount;
    }
    
    public Integer getViewCount() {
        return viewCount;
    }
    
    public void setViewCount(Integer viewCount) {
        this.viewCount = viewCount;
    }
    
    public LocalDateTime getCreateTime() {
        return createTime;
    }
    
    public void setCreateTime(LocalDateTime createTime) {
        this.createTime = createTime;
    }
    
    public LocalDateTime getUpdateTime() {
        return updateTime;
    }
    
    public void setUpdateTime(LocalDateTime updateTime) {
        this.updateTime = updateTime;
    }
    
    public String getAuthorName() {
        return authorName;
    }
    
    public void setAuthorName(String authorName) {
        this.authorName = authorName;
    }
    
    public String getAuthorAvatar() {
        return authorAvatar;
    }
    
    public void setAuthorAvatar(String authorAvatar) {
        this.authorAvatar = authorAvatar;
    }
    
    @Override
    public String toString() {
        return "PostVO{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", authorName='" + authorName + '\'' +
                ", likeCount=" + likeCount +
                ", createTime=" + createTime +
                '}';
    }
}