package com.wisdomhub.interceptor;

import com.wisdomhub.context.UserContext;
import com.wisdomhub.exception.UnauthorizedException;
import com.wisdomhub.util.JwtUtil;
import io.jsonwebtoken.Claims;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * JWT 拦截器（请求进入 Controller 前解析 Token，注入用户信息）
 */
@Component
public class JwtInterceptor implements HandlerInterceptor {
    
    private static final Logger log = LoggerFactory.getLogger(JwtInterceptor.class);
    private static final String AUTH_HEADER = "Authorization";
    private static final String BEARER_PREFIX = "Bearer ";
    
    private final JwtUtil jwtUtil;
    
    public JwtInterceptor(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }
    
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        String authHeader = request.getHeader(AUTH_HEADER);
        
        // 如果没有 Authorization 头，允许通过（部分接口可能公开）
        if (!StringUtils.hasText(authHeader) || !authHeader.startsWith(BEARER_PREFIX)) {
            return true;
        }
        
        String token = authHeader.substring(BEARER_PREFIX.length());
        
        try {
            // 解析 JWT Token
            Claims claims = jwtUtil.parseToken(token);
            Long userId = jwtUtil.getUserIdFromClaims(claims);
            String email = jwtUtil.getEmailFromClaims(claims);
            
            if (userId == null || !StringUtils.hasText(email)) {
                throw new UnauthorizedException("Token 无效（缺少用户信息）");
            }
            
            // 存入 ThreadLocal
            UserContext.setUserId(userId);
            UserContext.setUserEmail(email);
            
            log.debug("JWT 解析成功: userId={}, email={}", userId, email);
            
        } catch (UnauthorizedException e) {
            // 直接抛出，由全局异常处理器返回 401
            throw e;
        } catch (Exception e) {
            log.error("JWT 解析异常", e);
            throw new UnauthorizedException("Token 解析失败");
        }
        
        return true;
    }
    
    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, 
                                Object handler, Exception ex) {
        // 清理 ThreadLocal，防止内存泄漏
        UserContext.clear();
    }
}