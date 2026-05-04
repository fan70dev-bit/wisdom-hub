package com.wisdomhub.service.impl;

import com.wisdomhub.context.UserContext;
import com.wisdomhub.entity.Post;
import com.wisdomhub.entity.PostFavorite;
import com.wisdomhub.entity.User;
import com.wisdomhub.exception.BusinessException;
import com.wisdomhub.exception.UnauthorizedException;
import com.wisdomhub.mapper.PostFavoriteMapper;
import com.wisdomhub.mapper.PostMapper;
import com.wisdomhub.mapper.UserMapper;
import com.wisdomhub.service.PostFavoriteService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.Collections;
import java.util.List;

/**
 * 帖子收藏服务实现
 */
@Service
public class PostFavoriteServiceImpl implements PostFavoriteService {
    
    private static final Logger log = LoggerFactory.getLogger(PostFavoriteServiceImpl.class);
    
    private final PostFavoriteMapper postFavoriteMapper;
    private final PostMapper postMapper;
    private final UserMapper userMapper;
    
    public PostFavoriteServiceImpl(PostFavoriteMapper postFavoriteMapper,
                                  PostMapper postMapper,
                                  UserMapper userMapper) {
        this.postFavoriteMapper = postFavoriteMapper;
        this.postMapper = postMapper;
        this.userMapper = userMapper;
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean toggleFavorite(Long postId) {
        // 身份鉴权
        String email = UserContext.getUserEmail();
        if (!StringUtils.hasText(email)) {
            throw new UnauthorizedException("请先登录");
        }
        
        User user = userMapper.findByEmail(email);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        
        String userId = user.getAccountId();
        
        // 检查是否已收藏
        PostFavorite existingFavorite = postFavoriteMapper.findByPostIdAndUserId(postId, userId);
        
        if (existingFavorite != null) {
            // 已收藏，执行取消收藏
            postFavoriteMapper.delete(postId, userId);
            postMapper.decrementFavoriteCount(postId);
            log.info("取消收藏: postId={}, userId={}", postId, userId);
            return false;
        } else {
            // 未收藏，执行收藏
            postFavoriteMapper.insert(postId, userId);
            postMapper.incrementFavoriteCount(postId);
            log.info("收藏成功: postId={}, userId={}", postId, userId);
            return true;
        }
    }
    
    @Override
    public boolean isFavorited(Long postId, String userId) {
        if (!StringUtils.hasText(userId)) {
            return false;
        }
        
        PostFavorite favorite = postFavoriteMapper.findByPostIdAndUserId(postId, userId);
        return favorite != null;
    }
    
    @Override
    public List<Post> getFavoriteList() {
        // 身份鉴权
        String email = UserContext.getUserEmail();
        if (!StringUtils.hasText(email)) {
            throw new UnauthorizedException("请先登录");
        }
        
        User user = userMapper.findByEmail(email);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        
        String userId = user.getAccountId();
        
        // 查询收藏的帖子ID列表
        List<Long> postIds = postFavoriteMapper.findPostIdsByUserId(userId);
        
        if (postIds == null || postIds.isEmpty()) {
            return Collections.emptyList();
        }
        
        // 批量查询帖子详情
        return postMapper.findByIds(postIds);
    }
}