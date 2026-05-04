package com.wisdomhub.mapper;

import com.wisdomhub.entity.PostLike;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 帖子点赞 Mapper
 */
@Mapper
public interface PostLikeMapper {
    
    /**
     * 点赞（插入记录）
     */
    int insert(@Param("postId") Long postId, @Param("userId") String userId);
    
    /**
     * 取消点赞（删除记录）
     */
    int delete(@Param("postId") Long postId, @Param("userId") String userId);
    
    /**
     * 查询用户是否已点赞
     */
    PostLike findByPostIdAndUserId(@Param("postId") Long postId, @Param("userId") String userId);
    
    /**
     * 统计帖子点赞数
     */
    int countByPostId(@Param("postId") Long postId);
}