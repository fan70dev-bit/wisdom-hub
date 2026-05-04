package com.wisdomhub.service;

import com.wisdomhub.dto.AddCommentRequest;
import com.wisdomhub.dto.CommentVO;

import java.util.List;

/**
 * 评论服务接口
 */
public interface CommentService {
    
    /**
     * 发表评论
     * 
     * @param request 评论请求
     * @return 评论 ID
     */
    Long addComment(AddCommentRequest request);
    
    /**
     * 删除评论（级联软删除）
     * 
     * @param id 评论 ID
     * @return 是否删除成功
     */
    boolean deleteComment(Long id);
    
    /**
     * 获取某帖子的评论树（树形结构）
     * 
     * @param postId 帖子 ID
     * @return 评论树列表
     */
    List<CommentVO> getCommentTreeByPostId(Long postId);
}