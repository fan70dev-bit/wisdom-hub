package com.wisdomhub.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * 发表评论请求
 */
public class AddCommentRequest {
    
    /**
     * 帖子 ID
     */
    @NotNull(message = "帖子 ID 不能为空")
    private Long postId;
    
    /**
     * 评论内容
     */
    @NotBlank(message = "评论内容不能为空")
    private String content;
    
    /**
     * 父评论 ID（0 表示顶级评论）
     */
    private Long parentId;
    
    /**
     * 回复目标用户 ID（可选）
     */
    private String replyToUserId;
    
    public AddCommentRequest() {
    }
    
    public Long getPostId() {
        return postId;
    }
    
    public void setPostId(Long postId) {
        this.postId = postId;
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
    
    @Override
    public String toString() {
        return "AddCommentRequest{" +
                "postId=" + postId +
                ", content='" + content + '\'' +
                ", parentId=" + parentId +
                ", replyToUserId='" + replyToUserId + '\'' +
                '}';
    }
}