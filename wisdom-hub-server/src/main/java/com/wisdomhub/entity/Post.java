package com.wisdomhub.entity;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * 帖子实体类（扩展点赞、收藏统计）
 */
public class Post {
    
    private Long id;
    private String title;
    private String content;
    private String videoUrl;
    private Integer type;
    private Integer status;
    private String authorId;
    private String auditRemark;
    private String ipAddress;
    
    /**
     * 点赞数
     */
    private Integer likeCount;
    
    /**
     * 收藏数
     */
    private Integer favoriteCount;
    
    /**
     * 浏览次数
     */
    private Integer viewCount;
    
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    
    // 无参构造
    public Post() {
    }
    
    // 全参构造
    public Post(Long id, String title, String content, String videoUrl, Integer type,
                Integer status, String authorId, String auditRemark, String ipAddress,
                Integer likeCount, Integer favoriteCount, Integer viewCount,
                LocalDateTime createTime, LocalDateTime updateTime) {
        this.id = id;
        this.title = title;
        this.content = content;
        this.videoUrl = videoUrl;
        this.type = type;
        this.status = status;
        this.authorId = authorId;
        this.auditRemark = auditRemark;
        this.ipAddress = ipAddress;
        this.likeCount = likeCount;
        this.favoriteCount = favoriteCount;
        this.viewCount = viewCount;
        this.createTime = createTime;
        this.updateTime = updateTime;
    }
    
    public static PostBuilder builder() {
        return new PostBuilder();
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
    
    public String getAuditRemark() {
        return auditRemark;
    }
    
    public void setAuditRemark(String auditRemark) {
        this.auditRemark = auditRemark;
    }
    
    public String getIpAddress() {
        return ipAddress;
    }
    
    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
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
    
    @Override
    public String toString() {
        return "Post{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", likeCount=" + likeCount +
                ", favoriteCount=" + favoriteCount +
                ", viewCount=" + viewCount +
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
    
    /**
     * Builder 内部类
     */
    public static class PostBuilder {
        private Long id;
        private String title;
        private String content;
        private String videoUrl;
        private Integer type;
        private Integer status;
        private String authorId;
        private String auditRemark;
        private String ipAddress;
        private Integer likeCount;
        private Integer favoriteCount;
        private Integer viewCount;
        private LocalDateTime createTime;
        private LocalDateTime updateTime;
        
        PostBuilder() {
        }
        
        public PostBuilder id(Long id) {
            this.id = id;
            return this;
        }
        
        public PostBuilder title(String title) {
            this.title = title;
            return this;
        }
        
        public PostBuilder content(String content) {
            this.content = content;
            return this;
        }
        
        public PostBuilder videoUrl(String videoUrl) {
            this.videoUrl = videoUrl;
            return this;
        }
        
        public PostBuilder type(Integer type) {
            this.type = type;
            return this;
        }
        
        public PostBuilder status(Integer status) {
            this.status = status;
            return this;
        }
        
        public PostBuilder authorId(String authorId) {
            this.authorId = authorId;
            return this;
        }
        
        public PostBuilder auditRemark(String auditRemark) {
            this.auditRemark = auditRemark;
            return this;
        }
        
        public PostBuilder ipAddress(String ipAddress) {
            this.ipAddress = ipAddress;
            return this;
        }
        
        public PostBuilder likeCount(Integer likeCount) {
            this.likeCount = likeCount;
            return this;
        }
        
        public PostBuilder favoriteCount(Integer favoriteCount) {
            this.favoriteCount = favoriteCount;
            return this;
        }
        
        public PostBuilder viewCount(Integer viewCount) {
            this.viewCount = viewCount;
            return this;
        }
        
        public PostBuilder createTime(LocalDateTime createTime) {
            this.createTime = createTime;
            return this;
        }
        
        public PostBuilder updateTime(LocalDateTime updateTime) {
            this.updateTime = updateTime;
            return this;
        }
        
        public Post build() {
            return new Post(id, title, content, videoUrl, type, status, authorId,
                    auditRemark, ipAddress, likeCount, favoriteCount, viewCount,
                    createTime, updateTime);
        }
    }
}