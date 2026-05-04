package com.wisdomhub.mapper;

import com.wisdomhub.entity.PostFavorite;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 帖子收藏 Mapper
 */
@Mapper
public interface PostFavoriteMapper {
    
    /**
     * 收藏（插入记录）
     */
    int insert(@Param("postId") Long postId, @Param("userId") String userId);
    
    /**
     * 取消收藏（删除记录）
     */
    int delete(@Param("postId") Long postId, @Param("userId") String userId);
    
    /**
     * 查询用户是否已收藏
     */
    PostFavorite findByPostIdAndUserId(@Param("postId") Long postId, @Param("userId") String userId);
    
    /**
     * 统计帖子收藏数
     */
    int countByPostId(@Param("postId") Long postId);
    
    /**
     * 查询用户收藏列表
     */
    List<Long> findPostIdsByUserId(@Param("userId") String userId);
}