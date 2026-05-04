package com.wisdomhub.service;

import com.wisdomhub.entity.Post;

import java.util.List;

/**
 * 帖子收藏服务接口
 */
public interface PostFavoriteService {
    
    /**
     * 收藏/取消收藏（Toggle）
     * 
     * @param postId 帖子 ID
     * @return true-已收藏，false-已取消收藏
     */
    boolean toggleFavorite(Long postId);
    
    /**
     * 查询用户是否已收藏
     * 
     * @param postId 帖子 ID
     * @param userId 用户账号 ID
     * @return 是否已收藏
     */
    boolean isFavorited(Long postId, String userId);
    
    /**
     * 查询用户收藏列表
     * 
     * @return 收藏的帖子列表
     */
    List<Post> getFavoriteList();
}