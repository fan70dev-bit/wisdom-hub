package com.wisdomhub.service;

import com.wisdomhub.dto.LoginRequest;
import com.wisdomhub.dto.LoginResponse;
import com.wisdomhub.entity.User;
import com.wisdomhub.exception.BusinessException;
import com.wisdomhub.mapper.UserMapper;
import com.wisdomhub.service.impl.AuthServiceImpl;
import com.wisdomhub.util.EmailValidator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;

import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * 认证服务测试（移除 Lombok 版本）
 */
@DisplayName("认证服务测试")
class AuthServiceTest {

    // Mock 对象
    private JavaMailSender mailSender;
    private StringRedisTemplate redisTemplate;
    private ValueOperations<String, String> valueOperations;
    private UserMapper userMapper;
    private EmailValidator emailValidator;

    // 被测试对象
    private AuthServiceImpl authService;

    @BeforeEach
    void setUp() {
        // 手动创建 Mock 对象
        mailSender = mock(JavaMailSender.class);
        redisTemplate = mock(StringRedisTemplate.class);
        valueOperations = mock(ValueOperations.class);
        userMapper = mock(UserMapper.class);
        emailValidator = mock(EmailValidator.class);

        // 配置 Redis Mock
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);

        // 手动创建被测试对象（构造器注入）
        authService = new AuthServiceImpl(mailSender, redisTemplate, userMapper, emailValidator);

        // 手动设置配置参数（模拟 @Value 注入）
        authService.setVerifyCodeExpireMinutes(5);
        authService.setEmailIntervalSeconds(60);
        authService.setIpDailyLimit(10);
    }

    @Test
    @DisplayName("测试发送验证码 - 成功场景")
    void testSendVerifyCodeSuccess() {
        String email = "test@qq.com";
        String ip = "192.168.1.1";

        // Mock 行为
        doNothing().when(emailValidator).validate(email);
        when(redisTemplate.hasKey(anyString())).thenReturn(false);
        when(valueOperations.get(anyString())).thenReturn(null);
        doNothing().when(mailSender).send(any(SimpleMailMessage.class));

        // 执行测试
        assertDoesNotThrow(() -> authService.sendVerifyCode(email, ip));

        // 验证调用
        verify(emailValidator).validate(email);
        verify(mailSender).send(any(SimpleMailMessage.class));
        verify(valueOperations, atLeastOnce()).set(anyString(), anyString(), anyLong(), any(TimeUnit.class));
    }

    @Test
    @DisplayName("测试发送验证码 - 临时邮箱拦截")
    void testSendVerifyCodeDisposableEmail() {
        String email = "test@chacuo.net";
        String ip = "192.168.1.1";

        doThrow(new BusinessException("不支持临时邮箱，请使用常用邮箱"))
            .when(emailValidator).validate(email);

        BusinessException exception = assertThrows(
            BusinessException.class,
            () -> authService.sendVerifyCode(email, ip)
        );

        assertEquals("不支持临时邮箱，请使用常用邮箱", exception.getMessage());
        verify(mailSender, never()).send(any(SimpleMailMessage.class));
    }

    @Test
    @DisplayName("测试发送验证码 - 邮箱频率限制")
    void testSendVerifyCodeEmailRateLimit() {
        String email = "test@qq.com";
        String ip = "192.168.1.1";

        doNothing().when(emailValidator).validate(email);
        when(redisTemplate.hasKey(contains("email-rate"))).thenReturn(true);
        when(redisTemplate.getExpire(anyString(), any(TimeUnit.class))).thenReturn(45L);

        BusinessException exception = assertThrows(
            BusinessException.class,
            () -> authService.sendVerifyCode(email, ip)
        );

        assertTrue(exception.getMessage().contains("发送过于频繁"));
        verify(mailSender, never()).send(any(SimpleMailMessage.class));
    }

    @Test
    @DisplayName("测试发送验证码 - IP每日限制")
    void testSendVerifyCodeIpDailyLimit() {
        String email = "test@qq.com";
        String ip = "192.168.1.1";

        doNothing().when(emailValidator).validate(email);
        when(redisTemplate.hasKey(contains("email-rate"))).thenReturn(false);
        when(valueOperations.get(contains("ip-rate"))).thenReturn("10");

        BusinessException exception = assertThrows(
            BusinessException.class,
            () -> authService.sendVerifyCode(email, ip)
        );

        assertEquals("今日发送次数已达上限，请明天再试", exception.getMessage());
    }

    @Test
    @DisplayName("测试登录 - 新用户注册")
    void testLoginNewUser() {
        // 手动构建请求对象
        LoginRequest request = new LoginRequest();
        request.setEmail("newuser@qq.com");
        request.setCode("123456");
        String ip = "192.168.1.1";

        doNothing().when(emailValidator).validate(request.getEmail());
        when(valueOperations.get(contains("verify-code"))).thenReturn("123456");
        when(userMapper.findByEmail(request.getEmail())).thenReturn(null);
        
        // Mock insert 方法，模拟设置自增ID
        when(userMapper.insert(any(User.class))).thenAnswer(invocation -> {
            User user = invocation.getArgument(0);
            user.setId(1L);
            return 1;
        });

        LoginResponse response = authService.login(request, ip);

        assertNotNull(response);
        assertTrue(response.getIsNewUser());
        assertEquals("newuser@qq.com", response.getEmail());
        assertEquals("newuser", response.getNickname());
        assertNotNull(response.getToken());

        verify(userMapper).insert(any(User.class));
        verify(redisTemplate).delete(contains("verify-code"));
    }

    @Test
    @DisplayName("测试登录 - 已有用户")
    void testLoginExistingUser() {
        // 手动构建请求对象
        LoginRequest request = new LoginRequest();
        request.setEmail("existing@qq.com");
        request.setCode("123456");
        String ip = "192.168.1.1";

        // 手动构建已存在用户对象
        User existingUser = User.builder()
            .id(1L)
            .email("existing@qq.com")
            .nickname("existing")
            .status(0)
            .createTime(LocalDateTime.now())
            .build();

        doNothing().when(emailValidator).validate(request.getEmail());
        when(valueOperations.get(contains("verify-code"))).thenReturn("123456");
        when(userMapper.findByEmail(request.getEmail())).thenReturn(existingUser);

        LoginResponse response = authService.login(request, ip);

        assertNotNull(response);
        assertFalse(response.getIsNewUser());
        assertEquals(1L, response.getUserId());

        verify(userMapper).updateLastLogin(any(User.class));
        verify(userMapper, never()).insert(any(User.class));
    }

    @Test
    @DisplayName("测试登录 - 验证码错误")
    void testLoginWrongCode() {
        LoginRequest request = new LoginRequest();
        request.setEmail("test@qq.com");
        request.setCode("000000");
        String ip = "192.168.1.1";

        doNothing().when(emailValidator).validate(request.getEmail());
        when(valueOperations.get(contains("verify-code"))).thenReturn("123456");

        BusinessException exception = assertThrows(
            BusinessException.class,
            () -> authService.login(request, ip)
        );

        assertEquals("验证码错误", exception.getMessage());
    }

    @Test
    @DisplayName("测试登录 - 验证码过期")
    void testLoginExpiredCode() {
        LoginRequest request = new LoginRequest();
        request.setEmail("test@qq.com");
        request.setCode("123456");
        String ip = "192.168.1.1";

        doNothing().when(emailValidator).validate(request.getEmail());
        when(valueOperations.get(contains("verify-code"))).thenReturn(null);

        BusinessException exception = assertThrows(
            BusinessException.class,
            () -> authService.login(request, ip)
        );

        assertEquals("验证码已过期，请重新获取", exception.getMessage());
    }

    @Test
    @DisplayName("测试邮件发送失败处理")
    void testSendEmailFailure() {
        String email = "test@qq.com";
        String ip = "192.168.1.1";

        doNothing().when(emailValidator).validate(email);
        when(redisTemplate.hasKey(anyString())).thenReturn(false);
        when(valueOperations.get(anyString())).thenReturn(null);
        
        // Mock 邮件发送异常
        doThrow(new RuntimeException("SMTP服务器连接失败"))
            .when(mailSender).send(any(SimpleMailMessage.class));

        BusinessException exception = assertThrows(
            BusinessException.class,
            () -> authService.sendVerifyCode(email, ip)
        );

        assertEquals("邮件发送失败，请稍后重试", exception.getMessage());
    }
}