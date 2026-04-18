package com.wisdomhub.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Redis Key 工具测试
 */
@DisplayName("Redis Key 工具测试")
class RedisKeyUtilTest {

    @Test
    @DisplayName("测试验证码Key生成")
    void testGetVerifyCodeKey() {
        String key = RedisKeyUtil.getVerifyCodeKey("test@qq.com");
        assertEquals("wisdom-hub:verify-code:test@qq.com", key);
    }

    @Test
    @DisplayName("测试邮箱限流Key生成")
    void testGetEmailRateLimitKey() {
        String key = RedisKeyUtil.getEmailRateLimitKey("test@qq.com");
        assertEquals("wisdom-hub:email-rate:test@qq.com", key);
    }

    @Test
    @DisplayName("测试IP限流Key生成")
    void testGetIpRateLimitKey() {
        String key = RedisKeyUtil.getIpRateLimitKey("192.168.1.1");
        assertEquals("wisdom-hub:ip-rate:192.168.1.1", key);
    }

    @Test
    @DisplayName("测试Key唯一性")
    void testKeyUniqueness() {
        String key1 = RedisKeyUtil.getVerifyCodeKey("user1@qq.com");
        String key2 = RedisKeyUtil.getVerifyCodeKey("user2@qq.com");
        assertNotEquals(key1, key2);
    }
}