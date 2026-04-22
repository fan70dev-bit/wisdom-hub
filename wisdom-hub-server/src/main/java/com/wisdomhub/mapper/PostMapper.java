package com.wisdomhub.mapper;

import com.wisdomhub.entity.Post;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 文章/碎碎念数据访问层
 */
@Mapper
public interface PostMapper {
    
    /**
     * 创建文章/碎碎念
     */
    int insert(Post post);
    
    /**
     * 根据ID查询
     */
    Post findById(@Param("id") Long id);
    
    /**
     * 查询“我的花园”列表（根据作者邮箱）
     * 如果 includePrivate=true 则包含私密；否则仅公开/按业务规则
     */
    List<Post> findGardenByAuthorEmail(@Param("authorEmail") String authorEmail,
                                       @Param("includePrivate") Boolean includePrivate);
    
    /**
     * 查询“广场”列表（仅公开）
     */
    List<Post> findPlaza();
    
    /**
     * 更新文章（标题/内容/封面/视频/可见性/类型/更新时间）
     */
    int update(Post post);
    
    /**
     * 删除文章（按ID）
     */
    int deleteById(@Param("id") Long id);
}