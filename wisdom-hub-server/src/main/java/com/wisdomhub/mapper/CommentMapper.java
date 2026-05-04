package com.wisdomhub.mapper;

import com.wisdomhub.dto.CommentVO;
import com.wisdomhub.entity.Comment;
import org.apache.ibatis.annotations.*;

import java.util.List;

/**
 * 评论数据访问层
 */
@Mapper
public interface CommentMapper {
    
    /**
     * 新增评论
     */
    @Insert("INSERT INTO tb_comment (post_id, user_id, content, parent_id, reply_to_user_id, status, create_time) " +
            "VALUES (#{postId}, #{userId}, #{content}, #{parentId}, #{replyToUserId}, #{status}, #{createTime})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(Comment comment);
    
    /**
     * 根据 ID 查询评论
     */
    @Select("SELECT * FROM tb_comment WHERE id = #{id}")
    Comment findById(@Param("id") Long id);
    
    /**
     * 查询某帖子的所有正常评论（联表查询作者信息）
     * 
     * @param postId 帖子 ID
     * @return 评论列表（包含作者昵称、头像、回复目标用户昵称）
     */
    @Select("SELECT " +
            "c.id, c.post_id AS postId, c.user_id AS userId, c.content, c.parent_id AS parentId, " +
            "c.reply_to_user_id AS replyToUserId, c.status, c.create_time AS createTime, " +
            "u1.username AS authorName, u1.avatar_url AS authorAvatar, " +
            "u2.username AS replyToUserName " +
            "FROM tb_comment c " +
            "LEFT JOIN tb_user u1 ON c.user_id = u1.account_id " +
            "LEFT JOIN tb_user u2 ON c.reply_to_user_id = u2.account_id " +
            "WHERE c.post_id = #{postId} AND c.status = 0 " +
            "ORDER BY c.create_time ASC")
    List<CommentVO> findByPostId(@Param("postId") Long postId);
    
    /**
     * 逻辑删除评论（单条）
     */
    @Update("UPDATE tb_comment SET status = 1 WHERE id = #{id}")
    int logicDelete(@Param("id") Long id);
    
    /**
     * 查询某评论的所有子评论（用于递归删除）
     */
    @Select("SELECT * FROM tb_comment WHERE parent_id = #{parentId}")
    List<Comment> findChildren(@Param("parentId") Long parentId);
    
    /**
     * MySQL 8.0 递归 CTE：级联逻辑删除（推荐）
     * 
     * 使用 WITH RECURSIVE 一次性更新所有后代评论的 status
     * 
     * @param id 要删除的评论 ID
     */
    @Update("<script>" +
            "WITH RECURSIVE comment_tree AS ( " +
            "  SELECT id FROM tb_comment WHERE id = #{id} " +
            "  UNION ALL " +
            "  SELECT c.id FROM tb_comment c " +
            "  INNER JOIN comment_tree ct ON c.parent_id = ct.id " +
            ") " +
            "UPDATE tb_comment SET status = 1 WHERE id IN (SELECT id FROM comment_tree)" +
            "</script>")
    int cascadeLogicDelete(@Param("id") Long id);
}