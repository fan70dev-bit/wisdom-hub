package com.wisdomhub.entity;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * 帖子点赞记录
 */
public class PostLike {
    
    private Long id;
    private Long postId;
    private String userId;
    private LocalDateTime createTime;
    
    public PostLike() {
    }
    
    public PostLike(Long id, Long postId, String userId, LocalDateTime createTime) {
        this.id = id;
        this.postId = postId;
        this.userId = userId;
        this.createTime = createTime;
    }
    
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public Long getPostId() {
        return postId;
    }
    
    public void setPostId(Long postId) {
        this.postId = postId;
    }
    
    public String getUserId() {
        return userId;
    }
    
    public void setUserId(String userId) {
        this.userId = userId;
    }
    
    public LocalDateTime getCreateTime() {
        return createTime;
    }
    
    public void setCreateTime(LocalDateTime createTime) {
        this.createTime = createTime;
    }
    
    @Override
    public String toString() {
        return "PostLike{" +
                "id=" + id +
                ", postId=" + postId +
                ", userId='" + userId + '\'' +
                ", createTime=" + createTime +
                '}';
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PostLike postLike = (PostLike) o;
        return Objects.equals(id, postLike.id);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}