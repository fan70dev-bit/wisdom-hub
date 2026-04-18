package com.wisdomhub.mapper;

import com.wisdomhub.entity.User;
import org.apache.ibatis.annotations.*;

/**
 * 用户数据访问层
 */
@Mapper
public interface UserMapper {

    /**
     * 根据邮箱查询用户
     */
    @Select("SELECT * FROM tb_user WHERE email = #{email}")
    User findByEmail(@Param("email") String email);

    /**
     * 创建新用户
     */
    @Insert("INSERT INTO tb_user (email, nickname, status, create_time, last_login_time, last_login_ip) " +
            "VALUES (#{email}, #{nickname}, #{status}, #{createTime}, #{lastLoginTime}, #{lastLoginIp})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(User user);

    /**
     * 更新最后登录信息
     */
    @Update("UPDATE tb_user SET last_login_time = #{lastLoginTime}, last_login_ip = #{lastLoginIp} " +
            "WHERE id = #{id}")
    int updateLastLogin(User user);
}