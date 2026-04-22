package com.wisdomhub.service.impl;

import com.wisdomhub.dto.LoginRequest;
import com.wisdomhub.dto.LoginResponse;
import com.wisdomhub.entity.User;
import com.wisdomhub.exception.BusinessException;
import com.wisdomhub.mapper.UserMapper;
import com.wisdomhub.service.AuthService;
import com.wisdomhub.util.EmailValidator;
import com.wisdomhub.util.JwtUtil;
import com.wisdomhub.util.RedisKeyUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * 认证服务实现
 */
@Service
public class AuthServiceImpl implements AuthService {

    private static final Logger log = LoggerFactory.getLogger(AuthServiceImpl.class);

    private final JavaMailSender mailSender;
    private final StringRedisTemplate redisTemplate;
    private final UserMapper userMapper;
    private final EmailValidator emailValidator;
    private final JwtUtil jwtUtil;  // 新增注入

    @Value("${spring.mail.username}")
    private String mailFrom;

    @Value("${app.security.verify-code.expire-minutes:5}")
    private Integer verifyCodeExpireMinutes;

    @Value("${app.security.rate-limit.email-interval-seconds:60}")
    private Integer emailIntervalSeconds;

    @Value("${app.security.rate-limit.ip-daily-limit:10}")
    private Integer ipDailyLimit;

    @Autowired
    public AuthServiceImpl(JavaMailSender mailSender,
                           StringRedisTemplate redisTemplate,
                           UserMapper userMapper,
                           EmailValidator emailValidator,
                           JwtUtil jwtUtil) {  // 新增构造器注入
        this.mailSender = mailSender;
        this.redisTemplate = redisTemplate;
        this.userMapper = userMapper;
        this.emailValidator = emailValidator;
        this.jwtUtil = jwtUtil;
    }

    @Override
    public void sendVerifyCode(String email, String clientIp) {
        // 1. 邮箱格式及黑名单校验
        emailValidator.validate(email);

        // 2. 检查邮箱发送频率（60秒限制）
        String emailRateKey = RedisKeyUtil.getEmailRateLimitKey(email);
        if (Boolean.TRUE.equals(redisTemplate.hasKey(emailRateKey))) {
            Long ttl = redisTemplate.getExpire(emailRateKey, TimeUnit.SECONDS);
            throw new BusinessException(String.format("发送过于频繁，请%d秒后再试", ttl));
        }

        // 3. 检查IP每日发送次数（10次限制）
        String ipRateKey = RedisKeyUtil.getIpRateLimitKey(clientIp);
        String ipCountStr = redisTemplate.opsForValue().get(ipRateKey);
        int ipCount = ipCountStr != null ? Integer.parseInt(ipCountStr) : 0;

        if (ipCount >= ipDailyLimit) {
            throw new BusinessException("今日发送次数已达上限，请明天再试");
        }

        // 4. 生成6位验证码
        String code = generateVerifyCode();

        // 5. 发送邮件
        try {
            sendEmail(email, code);
        } catch (Exception e) {
            log.error("邮件发送失败: email={}, error={}", email, e.getMessage(), e);
            throw new BusinessException("邮件发送失败，请稍后重试");
        }

        // 6. 保存验证码到Redis（5分钟有效期）
        String codeKey = RedisKeyUtil.getVerifyCodeKey(email);
        redisTemplate.opsForValue().set(codeKey, code, verifyCodeExpireMinutes, TimeUnit.MINUTES);

        // 7. 设置邮箱发送频率限制
        redisTemplate.opsForValue().set(emailRateKey, "1", emailIntervalSeconds, TimeUnit.SECONDS);

        // 8. 增加IP每日发送计数
        if (ipCountStr == null) {
            redisTemplate.opsForValue().set(ipRateKey, "1", 24, TimeUnit.HOURS);
        } else {
            redisTemplate.opsForValue().increment(ipRateKey);
        }

        log.info("验证码发送成功: email={}, ip={}, code={}", email, clientIp, code);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public LoginResponse login(LoginRequest request, String clientIp) {
        String email = request.getEmail();
        String code = request.getCode();

        // 1. 邮箱格式校验
        emailValidator.validate(email);

        // 2. 验证码校验
        String codeKey = RedisKeyUtil.getVerifyCodeKey(email);
        String cachedCode = redisTemplate.opsForValue().get(codeKey);

        if (cachedCode == null) {
            throw new BusinessException("验证码已过期，请重新获取");
        }

        if (!cachedCode.equals(code)) {
            throw new BusinessException("验证码错误");
        }

        // 3. 查询用户是否存在
        User user = userMapper.findByEmail(email);
        boolean isNewUser = false;

        if (user == null) {
            // 自动注册
            user = createNewUser(email, clientIp);
            isNewUser = true;
            log.info("新用户注册: email={}, userId={}", email, user.getId());
        } else {
            // 更新登录信息
            user.setLastLoginTime(LocalDateTime.now());
            user.setLastLoginIp(clientIp);
            userMapper.updateLastLogin(user);
            log.info("用户登录: email={}, userId={}", email, user.getId());
        }

        // 4. 删除验证码（一次性使用）
        redisTemplate.delete(codeKey);

        // 5. 生成真正的 JWT Token（替换原来的 UUID）
        String token = jwtUtil.generateToken(user.getId(), user.getEmail());

        // 6. 构建响应
        return LoginResponse.builder()
                .userId(user.getId())
                .email(user.getEmail())
                .nickname(user.getNickname())
                .token(token)  // 现在是真正的 JWT
                .isNewUser(isNewUser)
                .loginTime(LocalDateTime.now())
                .build();
    }

    /**
     * 创建新用户
     */
    private User createNewUser(String email, String clientIp) {
        String nickname = email.substring(0, email.indexOf('@'));

        User user = User.builder()
                .email(email)
                .nickname(nickname)
                .status(0)
                .createTime(LocalDateTime.now())
                .lastLoginTime(LocalDateTime.now())
                .lastLoginIp(clientIp)
                .build();

        userMapper.insert(user);
        return user;
    }

    /**
     * 生成6位数字验证码
     */
    private String generateVerifyCode() {
        Random random = new Random();
        return String.format("%06d", random.nextInt(1000000));
    }

    /**
     * 发送邮件
     */
    private void sendEmail(String to, String code) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(mailFrom);
        message.setTo(to);
        message.setSubject("【Wisdom Hub】邮箱验证码");
        message.setText(String.format(
                "您的验证码是：%s\n\n" +
                        "有效期为%d分钟，请勿泄露给他人。\n\n" +
                        "如非本人操作，请忽略此邮件。\n\n" +
                        "—— Wisdom Hub 团队",
                code, verifyCodeExpireMinutes
        ));

        mailSender.send(message);
    }

    /**
     * 生成Token（简单模拟）
     */
    private String generateToken(User user) {
        return "wisdom-hub-" + user.getId() + "-" + UUID.randomUUID().toString();
    }

    // Getter/Setter for testing
    public Integer getVerifyCodeExpireMinutes() {
        return verifyCodeExpireMinutes;
    }

    public void setVerifyCodeExpireMinutes(Integer verifyCodeExpireMinutes) {
        this.verifyCodeExpireMinutes = verifyCodeExpireMinutes;
    }

    public Integer getEmailIntervalSeconds() {
        return emailIntervalSeconds;
    }

    public void setEmailIntervalSeconds(Integer emailIntervalSeconds) {
        this.emailIntervalSeconds = emailIntervalSeconds;
    }

    public Integer getIpDailyLimit() {
        return ipDailyLimit;
    }

    public void setIpDailyLimit(Integer ipDailyLimit) {
        this.ipDailyLimit = ipDailyLimit;
    }
}