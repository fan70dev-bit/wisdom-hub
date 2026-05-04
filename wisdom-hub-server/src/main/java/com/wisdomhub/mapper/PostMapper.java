package com.wisdomhub.mapper;

import com.wisdomhub.dto.PostVO;
import com.wisdomhub.entity.Post;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 帖子数据访问层（扩展探索广场和搜索）
 */
@Mapper
public interface PostMapper {
    
    // ========== 已有方法（保留） ==========
    int insert(Post post);
    Post findById(@Param("id") Long id);
    List<Post> findGardenByAuthorId(@Param("authorId") String authorId);
    List<Post> findPlaza();
    int update(Post post);
    int logicDelete(@Param("id") Long id);
    int incrementLikeCount(@Param("id") Long id);
    int decrementLikeCount(@Param("id") Long id);
    int incrementFavoriteCount(@Param("id") Long id);
    int decrementFavoriteCount(@Param("id") Long id);
    int incrementViewCount(@Param("id") Long id);
    List<Post> findByIds(@Param("ids") List<Long> ids);
    
    // ========== 新增方法 ==========
    
    /**
     * 探索广场：分页查询公开帖子（按时间倒序）
     * 
     * @param offset 偏移量（LIMIT offset, pageSize）
     * @param pageSize 每页大小
     * @return 帖子列表（包含作者信息）
     */
    @Select("SELECT " +
            "p.id, p.title, p.content, p.video_url AS videoUrl, p.type, p.status, " +
            "p.author_id AS authorId, p.like_count AS likeCount, p.favorite_count AS favoriteCount, " +
            "p.view_count AS viewCount, p.create_time AS createTime, p.update_time AS updateTime, " +
            "u.username AS authorName, u.avatar_url AS authorAvatar " +
            "FROM tb_post p " +
            "INNER JOIN tb_user u ON p.author_id = u.account_id " +
            "WHERE p.status = 0 AND u.status = 0 " +
            "ORDER BY p.create_time DESC " +
            "LIMIT #{offset}, #{pageSize}")
    List<PostVO> findExploreList(@Param("offset") int offset, @Param("pageSize") int pageSize);
    
    /**
     * 探索广场：统计总数
     */
    @Select("SELECT COUNT(*) FROM tb_post p " +
            "INNER JOIN tb_user u ON p.author_id = u.account_id " +
            "WHERE p.status = 0 AND u.status = 0")
    int countExploreList();
    
    /**
     * 关键字搜索：分页查询（标题或内容包含关键字）
     * 
     * @param keyword 搜索关键字
     * @param offset 偏移量
     * @param pageSize 每页大小
     * @return 帖子列表（包含作者信息）
     */
    @Select("SELECT " +
            "p.id, p.title, p.content, p.video_url AS videoUrl, p.type, p.status, " +
            "p.author_id AS authorId, p.like_count AS likeCount, p.favorite_count AS favoriteCount, " +
            "p.view_count AS viewCount, p.create_time AS createTime, p.update_time AS updateTime, " +
            "u.username AS authorName, u.avatar_url AS authorAvatar " +
            "FROM tb_post p " +
            "INNER JOIN tb_user u ON p.author_id = u.account_id " +
            "WHERE p.status = 0 AND u.status = 0 " +
            "AND (p.title LIKE CONCAT('%', #{keyword}, '%') OR p.content LIKE CONCAT('%', #{keyword}, '%')) " +
            "ORDER BY p.create_time DESC " +
            "LIMIT #{offset}, #{pageSize}")
    List<PostVO> searchPosts(@Param("keyword") String keyword, 
                             @Param("offset") int offset, 
                             @Param("pageSize") int pageSize);
    
    /**
     * 关键字搜索：统计总数
     */
    @Select("SELECT COUNT(*) FROM tb_post p " +
            "INNER JOIN tb_user u ON p.author_id = u.account_id " +
            "WHERE p.status = 0 AND u.status = 0 " +
            "AND (p.title LIKE CONCAT('%', #{keyword}, '%') OR p.content LIKE CONCAT('%', #{keyword}, '%'))")
    int countSearchPosts(@Param("keyword") String keyword);
}