package com.wisdomhub.util;

/**
 * Redis Key 工具类
 */
public class RedisKeyUtil {

    private static final String PREFIX = "wisdom-hub:";

    private RedisKeyUtil() {
        // 工具类私有化构造
    }

    /**
     * 验证码Key
     */
    public static String getVerifyCodeKey(String email) {
        return PREFIX + "verify-code:" + email;
    }

    /**
     * 邮箱发送频率限制Key
     */
    public static String getEmailRateLimitKey(String email) {
        return PREFIX + "email-rate:" + email;
    }

    /**
     * IP发送频率限制Key
     */
    public static String getIpRateLimitKey(String ip) {
        return PREFIX + "ip-rate:" + ip;
    }
}