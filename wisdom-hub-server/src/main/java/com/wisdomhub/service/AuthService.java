package com.wisdomhub.service;

import com.wisdomhub.dto.LoginRequest;
import com.wisdomhub.dto.LoginResponse;

/**
 * 认证服务接口
 */
public interface AuthService {
    
    /**
     * 发送验证码
     * @param email 邮箱
     * @param clientIp 客户端IP
     */
    void sendVerifyCode(String email, String clientIp);
    
    /**
     * 邮箱验证码登录/注册
     * @param request 登录请求
     * @param clientIp 客户端IP
     * @return 登录响应
     */
    LoginResponse login(LoginRequest request, String clientIp);
}