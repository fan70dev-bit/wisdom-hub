package com.wisdomhub.integretion;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.wisdomhub.dto.LoginRequest;
import com.wisdomhub.dto.SendCodeRequest;
import com.wisdomhub.entity.User;
import com.wisdomhub.mapper.UserMapper;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * 认证流程集成测试
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DisplayName("认证流程集成测试")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class AuthIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Autowired
    private UserMapper userMapper;

    private static final String TEST_EMAIL = "integration.test@qq.com";

    @BeforeEach
    void setUp() {
        // 清理测试数据
        User user = userMapper.findByEmail(TEST_EMAIL);
        if (user != null) {
            // 需要添加删除方法: userMapper.deleteByEmail(TEST_EMAIL);
        }
        
        // 清理 Redis
        redisTemplate.keys("wisdom-hub:*").forEach(key -> redisTemplate.delete(key));
    }

    @Test
    @Order(1)
    @DisplayName("完整流程测试 - 发送验证码 -> 新用户注册")
    void testCompleteFlowNewUser() throws Exception {
        // 1. 构建发送验证码请求
        SendCodeRequest sendCodeRequest = new SendCodeRequest();
        sendCodeRequest.setEmail(TEST_EMAIL);

        mockMvc.perform(post("/api/auth/send-code")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(sendCodeRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("验证码已发送，请查收邮件"));

        // 2. 从Redis获取验证码
        String codeKey = "wisdom-hub:verify-code:" + TEST_EMAIL;
        String code = redisTemplate.opsForValue().get(codeKey);
        Assertions.assertNotNull(code);

        // 3. 构建登录请求
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setEmail(TEST_EMAIL);
        loginRequest.setCode(code);

        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("注册成功"))
                .andExpect(jsonPath("$.data.email").value(TEST_EMAIL))
                .andExpect(jsonPath("$.data.isNewUser").value(true))
                .andExpect(jsonPath("$.data.token").exists());

        // 4. 验证数据库
        User user = userMapper.findByEmail(TEST_EMAIL);
        Assertions.assertNotNull(user);
        Assertions.assertEquals(TEST_EMAIL, user.getEmail());
    }

    @Test
    @Order(2)
    @DisplayName("频率限制测试 - 同一邮箱60秒限制")
    void testEmailRateLimit() throws Exception {
        SendCodeRequest request = new SendCodeRequest();
        request.setEmail(TEST_EMAIL);

        // 第一次发送成功
        mockMvc.perform(post("/api/auth/send-code")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        // 第二次发送失败（频率限制）
        mockMvc.perform(post("/api/auth/send-code")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message", containsString("发送过于频繁")));
    }

    @Test
    @Order(3)
    @DisplayName("参数校验测试 - 无效邮箱格式")
    void testInvalidEmailFormat() throws Exception {
        SendCodeRequest request = new SendCodeRequest();
        request.setEmail("invalid-email");

        mockMvc.perform(post("/api/auth/send-code")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    @Order(4)
    @DisplayName("参数校验测试 - 空邮箱")
    void testEmptyEmail() throws Exception {
        SendCodeRequest request = new SendCodeRequest();
        request.setEmail("");

        mockMvc.perform(post("/api/auth/send-code")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @Order(5)
    @DisplayName("登录测试 - 验证码格式错误")
    void testInvalidCodeFormat() throws Exception {
        LoginRequest request = new LoginRequest();
        request.setEmail("test@qq.com");
        request.setCode("abc"); // 非6位数字

        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("验证码格式不正确"));
    }
}