package com.wisdomhub.mapper;

import com.wisdomhub.entity.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 用户数据访问层
 */
@Mapper
public interface UserMapper {
    
    /**
     * 根据邮箱查询用户
     */
    User findByEmail(@Param("email") String email);
    
    /**
     * 根据账号 ID 查询用户
     */
    User findByAccountId(@Param("accountId") String accountId);
    
    /**
     * 创建新用户
     */
    int insert(User user);
    
    /**
     * 更新最后登录信息
     */
    int updateLastLogin(User user);
    
    /**
     * 更新用户资料（用户名、头像、更新时间）
     */
    int updateProfile(User user);
    
    /**
     * 账号注销（数据匿名化）
     */
    int deactivate(@Param("id") Long id, 
                   @Param("email") String anonymizedEmail,
                   @Param("username") String anonymizedUsername);
}