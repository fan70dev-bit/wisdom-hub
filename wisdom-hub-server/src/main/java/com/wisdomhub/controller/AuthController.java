package com.wisdomhub.controller;

import com.wisdomhub.dto.LoginRequest;
import com.wisdomhub.dto.LoginResponse;
import com.wisdomhub.dto.SendCodeRequest;
import com.wisdomhub.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 认证控制器
 */
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    @Autowired
    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    /**
     * 发送验证码
     */
    @PostMapping("/send-code")
    public Map<String, Object> sendCode(@Valid @RequestBody SendCodeRequest request,
                                        HttpServletRequest httpRequest) {

        String clientIp = getClientIp(httpRequest);
        authService.sendVerifyCode(request.getEmail(), clientIp);

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "验证码已发送，请查收邮件");
        return response;
    }

    /**
     * 登录/注册
     */
    @PostMapping("/login")
    public Map<String, Object> login(@Valid @RequestBody LoginRequest request,
                                     HttpServletRequest httpRequest) {

        String clientIp = getClientIp(httpRequest);
        LoginResponse loginResponse = authService.login(request, clientIp);

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", loginResponse.getIsNewUser() ? "注册成功" : "登录成功");
        response.put("data", loginResponse);
        return response;
    }

    /**
     * 获取客户端真实IP
     */
    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");

        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("X-Real-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }

        // 多级代理情况，取第一个IP
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }

        return ip;
    }
}