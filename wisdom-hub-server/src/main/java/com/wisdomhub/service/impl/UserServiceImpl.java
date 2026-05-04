package com.wisdomhub.service.impl;

import com.wisdomhub.context.UserContext;
import com.wisdomhub.dto.UpdateProfileRequest;
import com.wisdomhub.entity.User;
import com.wisdomhub.exception.BusinessException;
import com.wisdomhub.exception.UnauthorizedException;
import com.wisdomhub.mapper.UserMapper;
import com.wisdomhub.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.Duration;
import java.time.LocalDateTime;

/**
 * 用户服务实现
 */
@Service
public class UserServiceImpl implements UserService {
    
    private static final Logger log = LoggerFactory.getLogger(UserServiceImpl.class);
    
    /**
     * 资料修改冷却时间（12小时）
     */
    private static final long PROFILE_UPDATE_COOLDOWN_HOURS = 12;
    
    private final UserMapper userMapper;
    
    public UserServiceImpl(UserMapper userMapper) {
        this.userMapper = userMapper;
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateProfile(UpdateProfileRequest request) {
        // ========== 第一步：身份鉴权 ==========
        Long userId = UserContext.getUserId();
        String email = UserContext.getUserEmail();
        
        if (userId == null || !StringUtils.hasText(email)) {
            throw new UnauthorizedException("请先登录");
        }
        
        // ========== 第二步：查询当前用户 ==========
        User user = userMapper.findByEmail(email);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        
        // ========== 第三步：检查12小时冷却 ==========
        LocalDateTime lastUpdate = user.getLastProfileUpdate();
        if (lastUpdate != null) {
            Duration duration = Duration.between(lastUpdate, LocalDateTime.now());
            long hoursSinceLastUpdate = duration.toHours();
            
            if (hoursSinceLastUpdate < PROFILE_UPDATE_COOLDOWN_HOURS) {
                long remainingHours = PROFILE_UPDATE_COOLDOWN_HOURS - hoursSinceLastUpdate;
                throw new BusinessException(
                    String.format("修改太频繁，请在 %d 小时后再试", remainingHours)
                );
            }
        }
        
        // ========== 第四步：构建更新对象 ==========
        User updateUser = new User();
        updateUser.setId(userId);
        updateUser.setUsername(request.getUsername());
        updateUser.setAvatarUrl(
            StringUtils.hasText(request.getAvatarUrl()) 
                ? request.getAvatarUrl() 
                : user.getAvatarUrl()
        );
        updateUser.setLastProfileUpdate(LocalDateTime.now());
        
        // ========== 第五步：执行更新 ==========
        int rows = userMapper.updateProfile(updateUser);
        
        if (rows > 0) {
            log.info("用户资料更新成功: userId={}, username={}", userId, request.getUsername());
            return true;
        }
        
        return false;
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deactivate() {
        // ========== 第一步：身份鉴权 ==========
        Long userId = UserContext.getUserId();
        String email = UserContext.getUserEmail();
        
        if (userId == null || !StringUtils.hasText(email)) {
            throw new UnauthorizedException("请先登录");
        }
        
        // ========== 第二步：查询当前用户 ==========
        User user = userMapper.findByEmail(email);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        
        // ========== 第三步：数据匿名化处理 ==========
        // 生成匿名邮箱：deleted_用户ID_user
        String anonymizedEmail = "deleted_" + userId + "_user";
        String anonymizedUsername = "已注销用户";
        
        int rows = userMapper.deactivate(userId, anonymizedEmail, anonymizedUsername);
        
        if (rows > 0) {
            log.info("账号注销成功: userId={}, originalEmail={}", userId, email);
            return true;
        }
        
        return false;
    }
    
    @Override
    public User getByAccountId(String accountId) {
        if (!StringUtils.hasText(accountId)) {
            throw new BusinessException("账号 ID 不能为空");
        }
        
        User user = userMapper.findByAccountId(accountId);
        
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        
        return user;
    }
}