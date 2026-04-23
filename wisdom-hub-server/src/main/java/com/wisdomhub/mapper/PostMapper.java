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
     * 根据 ID 查询
     */
    Post findById(@Param("id") Long id);

    /**
     * 查询"我的花园"列表（根据作者邮箱）
     */
    List<Post> findGardenByAuthorEmail(@Param("authorEmail") String authorEmail,
                                       @Param("includePrivate") Boolean includePrivate);

    /**
     * 查询"广场"列表（仅公开）
     */
    List<Post> findPlaza();

    /**
     * 动态更新文章（仅更新非空字段）
     */
    int update(Post post);

    /**
     * 删除文章（按 ID）
     */
    int deleteById(@Param("id") Long id);
}