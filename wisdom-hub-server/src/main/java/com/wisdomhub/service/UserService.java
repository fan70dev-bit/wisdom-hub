package com.wisdomhub.service;

import com.wisdomhub.dto.UpdateProfileRequest;
import com.wisdomhub.entity.User;

/**
 * 用户服务接口
 */
public interface UserService {
    
    /**
     * 更新用户资料（用户名、头像）
     * 
     * @param request 更新请求
     * @return 是否更新成功
     */
    boolean updateProfile(UpdateProfileRequest request);
    
    /**
     * 账号注销（数据匿名化）
     * 
     * @return 是否注销成功
     */
    boolean deactivate();
    
    /**
     * 根据账号 ID 查询用户
     * 
     * @param accountId 账号 ID
     * @return 用户信息
     */
    User getByAccountId(String accountId);
}