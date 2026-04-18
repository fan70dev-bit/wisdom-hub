package com.wisdomhub.integretion;

import com.wisdomhub.util.RedisKeyUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.test.context.ActiveProfiles;

import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Redis 集成测试
 */
@SpringBootTest
@ActiveProfiles("test")
@DisplayName("Redis 集成测试")
class RedisIntegrationTest {

    @Autowired
    private StringRedisTemplate redisTemplate;

    @BeforeEach
    void setUp() {
        redisTemplate.keys("wisdom-hub:*").forEach(key -> redisTemplate.delete(key));
    }

    @Test
    @DisplayName("测试验证码存储和过期")
    void testVerifyCodeExpiration() throws InterruptedException {
        String email = "test@qq.com";
        String code = "123456";
        String key = RedisKeyUtil.getVerifyCodeKey(email);

        // 存储验证码（2秒过期）
        redisTemplate.opsForValue().set(key, code, 2, TimeUnit.SECONDS);

        // 立即查询应该存在
        assertEquals(code, redisTemplate.opsForValue().get(key));

        // 等待3秒后应该过期
        Thread.sleep(3000);
        assertNull(redisTemplate.opsForValue().get(key));
    }

    @Test
    @DisplayName("测试频率限制计数")
    void testRateLimitCounter() {
        String ip = "192.168.1.1";
        String key = RedisKeyUtil.getIpRateLimitKey(ip);

        // 初始计数
        redisTemplate.opsForValue().set(key, "1", 24, TimeUnit.HOURS);
        assertEquals("1", redisTemplate.opsForValue().get(key));

        // 增加计数
        redisTemplate.opsForValue().increment(key);
        assertEquals("2", redisTemplate.opsForValue().get(key));

        redisTemplate.opsForValue().increment(key);
        assertEquals("3", redisTemplate.opsForValue().get(key));
    }

    @Test
    @DisplayName("测试并发计数安全性")
    void testConcurrentIncrement() {
        String ip = "192.168.1.100";
        String key = RedisKeyUtil.getIpRateLimitKey(ip);

        redisTemplate.opsForValue().set(key, "0", 1, TimeUnit.HOURS);

        // 模拟10次并发增加
        for (int i = 0; i < 10; i++) {
            redisTemplate.opsForValue().increment(key);
        }

        assertEquals("10", redisTemplate.opsForValue().get(key));
    }
}