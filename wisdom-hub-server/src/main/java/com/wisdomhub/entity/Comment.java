package com.wisdomhub.entity;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * 评论实体类
 */
public class Comment {
    
    private Long id;
    
    /**
     * 帖子 ID
     */
    private Long postId;
    
    /**
     * 评论者账号 ID（关联 tb_user.account_id）
     */
    private String userId;
    
    /**
     * 评论内容
     */
    private String content;
    
    /**
     * 父评论 ID（0 表示顶级评论）
     */
    private Long parentId;
    
    /**
     * 回复目标用户 ID（用于显示"回复@某人"）
     */
    private String replyToUserId;
    
    /**
     * 状态：0-正常，1-已删除
     */
    private Integer status;
    
    /**
     * 创建时间
     */
    private LocalDateTime createTime;
    
    public Comment() {
    }
    
    public Comment(Long id, Long postId, String userId, String content, Long parentId,
                   String replyToUserId, Integer status, LocalDateTime createTime) {
        this.id = id;
        this.postId = postId;
        this.userId = userId;
        this.content = content;
        this.parentId = parentId;
        this.replyToUserId = replyToUserId;
        this.status = status;
        this.createTime = createTime;
    }
    
    // Getter/Setter
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
    
    public String getContent() {
        return content;
    }
    
    public void setContent(String content) {
        this.content = content;
    }
    
    public Long getParentId() {
        return parentId;
    }
    
    public void setParentId(Long parentId) {
        this.parentId = parentId;
    }
    
    public String getReplyToUserId() {
        return replyToUserId;
    }
    
    public void setReplyToUserId(String replyToUserId) {
        this.replyToUserId = replyToUserId;
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
    
    @Override
    public String toString() {
        return "Comment{" +
                "id=" + id +
                ", postId=" + postId +
                ", userId='" + userId + '\'' +
                ", content='" + content + '\'' +
                ", parentId=" + parentId +
                ", createTime=" + createTime +
                '}';
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Comment comment = (Comment) o;
        return Objects.equals(id, comment.id);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}