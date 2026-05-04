package com.wisdomhub.service.impl;

import com.wisdomhub.dto.LoginRequest;
import com.wisdomhub.dto.LoginResponse;
import com.wisdomhub.entity.User;
import com.wisdomhub.exception.BusinessException;
import com.wisdomhub.mapper.UserMapper;
import com.wisdomhub.service.AuthService;
import com.wisdomhub.util.EmailValidator;
import com.wisdomhub.util.RedisKeyUtil;
import com.wisdomhub.util.JwtUtil;
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
import java.util.concurrent.TimeUnit;

/**
 * 认证服务实现（重构版）
 */
@Service
public class AuthServiceImpl implements AuthService {
    
    private static final Logger log = LoggerFactory.getLogger(AuthServiceImpl.class);
    
    /**
     * 默认头像地址
     */
    private static final String DEFAULT_AVATAR_URL = 
        "https://java-test-with-ai.oss-cn-beijing.aliyuncs.com/assets/default-croco.png";
    
    private final JavaMailSender mailSender;
    private final StringRedisTemplate redisTemplate;
    private final UserMapper userMapper;
    private final EmailValidator emailValidator;
    private final JwtUtil jwtUtil;
    
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
                          JwtUtil jwtUtil) {
        this.mailSender = mailSender;
        this.redisTemplate = redisTemplate;
        this.userMapper = userMapper;
        this.emailValidator = emailValidator;
        this.jwtUtil = jwtUtil;
    }
    
    @Override
    public void sendVerifyCode(String email, String clientIp) {
        // 邮箱格式及黑名单校验
        emailValidator.validate(email);
        
        // 检查邮箱发送频率
        String emailRateKey = RedisKeyUtil.getEmailRateLimitKey(email);
        if (Boolean.TRUE.equals(redisTemplate.hasKey(emailRateKey))) {
            Long ttl = redisTemplate.getExpire(emailRateKey, TimeUnit.SECONDS);
            throw new BusinessException(String.format("发送过于频繁，请%d秒后再试", ttl));
        }
        
        // 检查IP每日发送次数
        String ipRateKey = RedisKeyUtil.getIpRateLimitKey(clientIp);
        String ipCountStr = redisTemplate.opsForValue().get(ipRateKey);
        int ipCount = ipCountStr != null ? Integer.parseInt(ipCountStr) : 0;
        
        if (ipCount >= ipDailyLimit) {
            throw new BusinessException("今日发送次数已达上限，请明天再试");
        }
        
        // 生成6位验证码
        String code = generateVerifyCode();
        
        // 发送邮件
        try {
            sendEmail(email, code);
        } catch (Exception e) {
            log.error("邮件发送失败: email={}, error={}", email, e.getMessage(), e);
            throw new BusinessException("邮件发送失败，请稍后重试");
        }
        
        // 保存验证码到Redis
        String codeKey = RedisKeyUtil.getVerifyCodeKey(email);
        redisTemplate.opsForValue().set(codeKey, code, verifyCodeExpireMinutes, TimeUnit.MINUTES);
        
        // 设置邮箱发送频率限制
        redisTemplate.opsForValue().set(emailRateKey, "1", emailIntervalSeconds, TimeUnit.SECONDS);
        
        // 增加IP每日发送计数
        if (ipCountStr == null) {
            redisTemplate.opsForValue().set(ipRateKey, "1", 24, TimeUnit.HOURS);
        } else {
            redisTemplate.opsForValue().increment(ipRateKey);
        }
        
        log.info("验证码发送成功: email={}, ip={}", email, clientIp);
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public LoginResponse login(LoginRequest request, String clientIp) {
        String email = request.getEmail();
        String code = request.getCode();
        
        // 邮箱格式校验
        emailValidator.validate(email);
        
        // 验证码校验
        String codeKey = RedisKeyUtil.getVerifyCodeKey(email);
        String cachedCode = redisTemplate.opsForValue().get(codeKey);
        
        if (cachedCode == null) {
            throw new BusinessException("验证码已过期，请重新获取");
        }
        
        if (!cachedCode.equals(code)) {
            throw new BusinessException("验证码错误");
        }
        
        // 查询用户是否存在
        User user = userMapper.findByEmail(email);
        boolean isNewUser = false;
        
        if (user == null) {
            // ========== 自动注册（生成账号 ID、默认用户名、默认头像） ==========
            user = createNewUser(email, clientIp);
            isNewUser = true;
            log.info("新用户注册: email={}, userId={}, accountId={}", 
                    email, user.getId(), user.getAccountId());
        } else {
            // 检查账号状态
            if (user.getStatus() == 2) {
                throw new BusinessException("该账号已注销，无法登录");
            }
            if (user.getStatus() == 1) {
                throw new BusinessException("账号已被封禁，请联系管理员");
            }
            
            // 更新登录信息
            user.setLastLoginTime(LocalDateTime.now());
            user.setLastLoginIp(clientIp);
            userMapper.updateLastLogin(user);
            log.info("用户登录: email={}, userId={}", email, user.getId());
        }
        
        // 删除验证码（一次性使用）
        redisTemplate.delete(codeKey);
        
        // 生成真正的 JWT Token
        String token = jwtUtil.generateToken(user.getId(), user.getEmail());
        
        // 构建响应
        return LoginResponse.builder()
                .userId(user.getId())
                .accountId(user.getAccountId())
                .email(user.getEmail())
                .username(user.getUsername())
                .avatarUrl(user.getAvatarUrl())
                .token(token)
                .isNewUser(isNewUser)
                .loginTime(LocalDateTime.now())
                .build();
    }
    
    /**
     * 创建新用户（注册时生成默认值）
     */
    private User createNewUser(String email, String clientIp) {
        // 生成8-10位随机账号 ID
        String accountId = generateAccountId();
        
        // 生成默认用户名：Wisdom用户_随机后缀
        String randomSuffix = String.valueOf(new Random().nextInt(9999));
        String username = "Wisdom用户_" + randomSuffix;
        
        // 提取邮箱前缀作为昵称（兼容旧版）
        String nickname = email.substring(0, email.indexOf('@'));
        
        User user = User.builder()
                .accountId(accountId)
                .email(email)
                .username(username)
                .nickname(nickname)
                .avatarUrl(DEFAULT_AVATAR_URL)
                .status(0)
                .createTime(LocalDateTime.now())
                .lastLoginTime(LocalDateTime.now())
                .lastLoginIp(clientIp)
                .build();
        
        userMapper.insert(user);
        return user;
    }
    
    /**
     * 生成8-10位随机账号 ID（确保唯一）
     */
    private String generateAccountId() {
        Random random = new Random();
        int length = 8 + random.nextInt(3); // 8-10位
        
        StringBuilder accountId = new StringBuilder();
        for (int i = 0; i < length; i++) {
            accountId.append(random.nextInt(10));
        }
        
        // 检查是否重复（理论上概率极低，但仍需保证）
        User existingUser = userMapper.findByAccountId(accountId.toString());
        if (existingUser != null) {
            return generateAccountId(); // 递归重新生成
        }
        
        return accountId.toString();
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
}