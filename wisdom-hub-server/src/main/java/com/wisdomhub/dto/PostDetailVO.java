package com.wisdomhub.dto;

import com.wisdomhub.entity.Post;

/**
 * 帖子详情视图对象（包含用户交互状态）
 */
public class PostDetailVO {
    
    /**
     * 帖子基本信息
     */
    private Post post;
    
    /**
     * 当前用户是否已点赞
     */
    private Boolean isLiked;
    
    /**
     * 当前用户是否已收藏
     */
    private Boolean isFavorited;
    
    public PostDetailVO() {
    }
    
    public PostDetailVO(Post post, Boolean isLiked, Boolean isFavorited) {
        this.post = post;
        this.isLiked = isLiked;
        this.isFavorited = isFavorited;
    }
    
    public Post getPost() {
        return post;
    }
    
    public void setPost(Post post) {
        this.post = post;
    }
    
    public Boolean getIsLiked() {
        return isLiked;
    }
    
    public void setIsLiked(Boolean isLiked) {
        this.isLiked = isLiked;
    }
    
    public Boolean getIsFavorited() {
        return isFavorited;
    }
    
    public void setIsFavorited(Boolean isFavorited) {
        this.isFavorited = isFavorited;
    }
    
    @Override
    public String toString() {
        return "PostDetailVO{" +
                "post=" + post +
                ", isLiked=" + isLiked +
                ", isFavorited=" + isFavorited +
                '}';
    }
}