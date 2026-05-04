package com.wisdomhub.controller;

import com.wisdomhub.dto.AddCommentRequest;
import com.wisdomhub.dto.CommentVO;
import com.wisdomhub.dto.Result;
import com.wisdomhub.service.CommentService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 评论控制器
 */
@RestController
@RequestMapping("/api/comment")
public class CommentController {
    
    private final CommentService commentService;
    
    public CommentController(CommentService commentService) {
        this.commentService = commentService;
    }
    
    /**
     * 发表评论
     * 
     * POST /api/comment
     */
    @PostMapping
    public Result<Long> addComment(@Valid @RequestBody AddCommentRequest request) {
        Long commentId = commentService.addComment(request);
        return Result.success("评论成功", commentId);
    }
    
    /**
     * 删除评论（级联软删除）
     * 
     * DELETE /api/comment/{id}
     */
    @DeleteMapping("/{id}")
    public Result<Void> deleteComment(@PathVariable Long id) {
        boolean success = commentService.deleteComment(id);
        
        if (success) {
            return Result.success("删除成功", null);
        } else {
            return Result.fail(500, "删除失败");
        }
    }
    
    /**
     * 获取某帖子的评论树
     * 
     * GET /api/comment/post/{postId}
     */
    @GetMapping("/post/{postId}")
    public Result<List<CommentVO>> getCommentTree(@PathVariable Long postId) {
        List<CommentVO> commentTree = commentService.getCommentTreeByPostId(postId);
        return Result.success(commentTree);
    }
}