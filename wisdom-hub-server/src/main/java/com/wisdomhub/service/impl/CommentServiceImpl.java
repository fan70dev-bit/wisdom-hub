package com.wisdomhub.service.impl;

import com.wisdomhub.context.UserContext;
import com.wisdomhub.dto.AddCommentRequest;
import com.wisdomhub.dto.CommentVO;
import com.wisdomhub.entity.Comment;
import com.wisdomhub.entity.User;
import com.wisdomhub.exception.BusinessException;
import com.wisdomhub.exception.ForbiddenException;
import com.wisdomhub.exception.UnauthorizedException;
import com.wisdomhub.mapper.CommentMapper;
import com.wisdomhub.mapper.UserMapper;
import com.wisdomhub.service.CommentService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 评论服务实现
 */
@Service
public class CommentServiceImpl implements CommentService {
    
    private static final Logger log = LoggerFactory.getLogger(CommentServiceImpl.class);
    
    private final CommentMapper commentMapper;
    private final UserMapper userMapper;
    
    public CommentServiceImpl(CommentMapper commentMapper, UserMapper userMapper) {
        this.commentMapper = commentMapper;
        this.userMapper = userMapper;
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long addComment(AddCommentRequest request) {
        // ========== 第一步：身份鉴权 ==========
        String email = UserContext.getUserEmail();
        if (!StringUtils.hasText(email)) {
            throw new UnauthorizedException("请先登录");
        }
        
        User user = userMapper.findByEmail(email);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        
        String userId = user.getAccountId();
        
        // ========== 第二步：内容校验 ==========
        if (!StringUtils.hasText(request.getContent())) {
            throw new BusinessException("评论内容不能为空");
        }
        
        if (request.getContent().length() > 500) {
            throw new BusinessException("评论内容不能超过 500 字");
        }
        
        // ========== 第三步：构建 Comment 对象 ==========
        Comment comment = new Comment();
        comment.setPostId(request.getPostId());
        comment.setUserId(userId);
        comment.setContent(request.getContent());
        comment.setParentId(request.getParentId() != null ? request.getParentId() : 0L);
        comment.setReplyToUserId(request.getReplyToUserId());
        comment.setStatus(0);
        comment.setCreateTime(LocalDateTime.now());
        
        // ========== 第四步：插入数据库 ==========
        int rows = commentMapper.insert(comment);
        if (rows <= 0) {
            throw new BusinessException("评论发表失败");
        }
        
        log.info("评论发表成功: id={}, postId={}, userId={}", comment.getId(), request.getPostId(), userId);
        
        return comment.getId();
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteComment(Long id) {
        // ========== 第一步：身份鉴权 ==========
        String email = UserContext.getUserEmail();
        if (!StringUtils.hasText(email)) {
            throw new UnauthorizedException("请先登录");
        }
        
        User user = userMapper.findByEmail(email);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        
        String currentUserId = user.getAccountId();
        
        // ========== 第二步：查询评论，校验权限 ==========
        Comment comment = commentMapper.findById(id);
        if (comment == null) {
            throw new BusinessException("评论不存在");
        }
        
        if (!Objects.equals(comment.getUserId(), currentUserId)) {
            throw new ForbiddenException("无权删除他人的评论");
        }
        
        // ========== 第三步：级联逻辑删除（使用 MySQL 8.0 递归 CTE） ==========
        // 方案 A：使用 MySQL 8.0 WITH RECURSIVE（推荐，一次 SQL 完成）
        int rows = commentMapper.cascadeLogicDelete(id);
        
        // 方案 B：Java 递归实现（兜底方案，MySQL 版本低于 8.0 时使用）
        // cascadeDeleteRecursive(id);
        
        log.info("评论级联删除成功: id={}, affectedRows={}", id, rows);
        
        return rows > 0;
    }
    
    /**
     * 方案 B：Java 递归级联删除（兜底方案）
     * 
     * 适用于 MySQL 5.7 或不支持 WITH RECURSIVE 的场景
     */
    private void cascadeDeleteRecursive(Long parentId) {
        // 删除当前评论
        commentMapper.logicDelete(parentId);
        
        // 查询所有子评论
        List<Comment> children = commentMapper.findChildren(parentId);
        
        // 递归删除子评论
        for (Comment child : children) {
            cascadeDeleteRecursive(child.getId());
        }
    }
    
    @Override
    public List<CommentVO> getCommentTreeByPostId(Long postId) {
        // ========== 第一步：查询所有正常评论（平铺） ==========
        List<CommentVO> allComments = commentMapper.findByPostId(postId);
        
        if (allComments == null || allComments.isEmpty()) {
            return new ArrayList<>();
        }
        
        // ========== 第二步：构建树形结构（在 Java 层组装） ==========
        return buildCommentTree(allComments);
    }
    
    /**
     * 将平铺的评论列表构建成树形结构
     * 
     * @param allComments 所有评论（平铺）
     * @return 树形评论列表（仅返回顶级评论，children 嵌套子评论）
     */
    private List<CommentVO> buildCommentTree(List<CommentVO> allComments) {
        // 构建 ID -> CommentVO 的映射
        Map<Long, CommentVO> commentMap = new HashMap<>();
        for (CommentVO comment : allComments) {
            commentMap.put(comment.getId(), comment);
        }
        
        // 收集顶级评论
        List<CommentVO> rootComments = new ArrayList<>();
        
        for (CommentVO comment : allComments) {
            Long parentId = comment.getParentId();
            
            if (parentId == null || parentId == 0) {
                // 顶级评论
                rootComments.add(comment);
            } else {
                // 子评论：找到父评论，添加到 children
                CommentVO parent = commentMap.get(parentId);
                if (parent != null) {
                    parent.getChildren().add(comment);
                }
            }
        }
        
        return rootComments;
    }
}