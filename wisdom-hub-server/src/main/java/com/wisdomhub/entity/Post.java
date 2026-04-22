package com.wisdomhub.entity;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * 文章/碎碎念实体类
 */
public class Post {
    
    /**
     * 主键ID
     */
    private Long id;
    
    /**
     * 标题（长文可选，碎碎念可为空）
     */
    private String title;
    
    /**
     * 内容（必填）
     */
    private String content;
    
    /**
     * 封面图（可选）
     */
    private String coverImage;
    
    /**
     * 视频链接（B站等，可选）
     */
    private String videoUrl;
    
    /**
     * 类型：0-长文，1-碎碎念
     */
    private Integer type;
    
    /**
     * 可见性：0-公开，1-私密
     */
    private Integer visibility;
    
    /**
     * 创建人邮箱（用于“我的花园”查询）
     */
    private String authorEmail;
    
    /**
     * 创建人ID（用于关联/审计）
     */
    private Long authorId;
    
    /**
     * 创建时间
     */
    private LocalDateTime createTime;
    
    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
    
    // 无参构造
    public Post() {
    }
    
    // 全参构造
    public Post(Long id, String title, String content, String coverImage, String videoUrl,
                Integer type, Integer visibility, String authorEmail, Long authorId,
                LocalDateTime createTime, LocalDateTime updateTime) {
        this.id = id;
        this.title = title;
        this.content = content;
        this.coverImage = coverImage;
        this.videoUrl = videoUrl;
        this.type = type;
        this.visibility = visibility;
        this.authorEmail = authorEmail;
        this.authorId = authorId;
        this.createTime = createTime;
        this.updateTime = updateTime;
    }
    
    // Getter / Setter
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
    
    public Integer getType() {
        return type;
    }
    
    public void setType(Integer type) {
        this.type = type;
    }
    
    public Integer getVisibility() {
        return visibility;
    }
    
    public void setVisibility(Integer visibility) {
        this.visibility = visibility;
    }
    
    public String getAuthorEmail() {
        return authorEmail;
    }
    
    public void setAuthorEmail(String authorEmail) {
        this.authorEmail = authorEmail;
    }
    
    public Long getAuthorId() {
        return authorId;
    }
    
    public void setAuthorId(Long authorId) {
        this.authorId = authorId;
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
    
    @Override
    public String toString() {
        return "Post{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", content='" + content + '\'' +
                ", coverImage='" + coverImage + '\'' +
                ", videoUrl='" + videoUrl + '\'' +
                ", type=" + type +
                ", visibility=" + visibility +
                ", authorEmail='" + authorEmail + '\'' +
                ", authorId=" + authorId +
                ", createTime=" + createTime +
                ", updateTime=" + updateTime +
                '}';
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Post post = (Post) o;
        return Objects.equals(id, post.id);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}