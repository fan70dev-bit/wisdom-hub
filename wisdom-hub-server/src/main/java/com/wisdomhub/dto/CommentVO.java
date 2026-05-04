package com.wisdomhub.dto;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 评论视图对象（树形结构）
 */
public class CommentVO {
    
    private Long id;
    private Long postId;
    private String userId;
    private String content;
    private Long parentId;
    private String replyToUserId;
    private Integer status;
    private LocalDateTime createTime;
    
    // ========== 扩展字段（来自 tb_user） ==========
    /**
     * 评论者昵称
     */
    private String authorName;
    
    /**
     * 评论者头像
     */
    private String authorAvatar;
    
    /**
     * 回复目标用户昵称（用于显示"回复@某人"）
     */
    private String replyToUserName;
    
    // ========== 树形结构 ==========
    /**
     * 子评论列表（嵌套）
     */
    private List<CommentVO> children;
    
    public CommentVO() {
        this.children = new ArrayList<>();
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
    
    public String getReplyToUserName() {
        return replyToUserName;
    }
    
    public void setReplyToUserName(String replyToUserName) {
        this.replyToUserName = replyToUserName;
    }
    
    public List<CommentVO> getChildren() {
        return children;
    }
    
    public void setChildren(List<CommentVO> children) {
        this.children = children;
    }
    
    @Override
    public String toString() {
        return "CommentVO{" +
                "id=" + id +
                ", authorName='" + authorName + '\'' +
                ", content='" + content + '\'' +
                ", children=" + (children != null ? children.size() : 0) +
                '}';
    }
}