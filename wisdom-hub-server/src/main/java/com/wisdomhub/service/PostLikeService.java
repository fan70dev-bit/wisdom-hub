package com.wisdomhub.service;

/**
 * 帖子点赞服务接口
 */
public interface PostLikeService {
    
    /**
     * 点赞/取消点赞（Toggle）
     * 
     * @param postId 帖子 ID
     * @return true-已点赞，false-已取消点赞
     */
    boolean toggleLike(Long postId);
    
    /**
     * 查询用户是否已点赞
     * 
     * @param postId 帖子 ID
     * @param userId 用户账号 ID
     * @return 是否已点赞
     */
    boolean isLiked(Long postId, String userId);
}