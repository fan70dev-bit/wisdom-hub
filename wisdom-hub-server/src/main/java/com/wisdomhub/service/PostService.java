package com.wisdomhub.service;

import com.wisdomhub.entity.Post;

import java.util.List;

/**
 * 文章/碎碎念服务接口
 */
public interface PostService {
    
    /**
     * 创建文章/碎碎念（自动识别当前用户）
     */
    Long create(Post post);
    
    /**
     * 我的花园列表
     */
    List<Post> getGarden();
    
    /**
     * 广场列表
     */
    List<Post> getPlaza();
}