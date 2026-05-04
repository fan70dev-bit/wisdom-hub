package com.wisdomhub.service.impl;

import com.wisdomhub.context.UserContext;
import com.wisdomhub.entity.PostLike;
import com.wisdomhub.entity.User;
import com.wisdomhub.exception.BusinessException;
import com.wisdomhub.exception.UnauthorizedException;
import com.wisdomhub.mapper.PostLikeMapper;
import com.wisdomhub.mapper.PostMapper;
import com.wisdomhub.mapper.UserMapper;
import com.wisdomhub.service.PostLikeService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

/**
 * 帖子点赞服务实现
 */
@Service
public class PostLikeServiceImpl implements PostLikeService {
    
    private static final Logger log = LoggerFactory.getLogger(PostLikeServiceImpl.class);
    
    private final PostLikeMapper postLikeMapper;
    private final PostMapper postMapper;
    private final UserMapper userMapper;
    
    public PostLikeServiceImpl(PostLikeMapper postLikeMapper,
                              PostMapper postMapper,
                              UserMapper userMapper) {
        this.postLikeMapper = postLikeMapper;
        this.postMapper = postMapper;
        this.userMapper = userMapper;
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean toggleLike(Long postId) {
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
        
        // 检查是否已点赞
        PostLike existingLike = postLikeMapper.findByPostIdAndUserId(postId, userId);
        
        if (existingLike != null) {
            // 已点赞，执行取消点赞
            postLikeMapper.delete(postId, userId);
            postMapper.decrementLikeCount(postId);
            log.info("取消点赞: postId={}, userId={}", postId, userId);
            return false;
        } else {
            // 未点赞，执行点赞
            postLikeMapper.insert(postId, userId);
            postMapper.incrementLikeCount(postId);
            log.info("点赞成功: postId={}, userId={}", postId, userId);
            return true;
        }
    }
    
    @Override
    public boolean isLiked(Long postId, String userId) {
        if (!StringUtils.hasText(userId)) {
            return false;
        }
        
        PostLike like = postLikeMapper.findByPostIdAndUserId(postId, userId);
        return like != null;
    }
}