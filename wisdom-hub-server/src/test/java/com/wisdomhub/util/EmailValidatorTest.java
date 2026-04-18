package com.wisdomhub.util;

import com.wisdomhub.exception.BusinessException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 邮箱验证器测试
 */
@DisplayName("邮箱验证器测试")
class EmailValidatorTest {

    private EmailValidator emailValidator;

    @BeforeEach
    void setUp() {
        emailValidator = new EmailValidator();
        // 手动设置黑名单（模拟 @Value 注入）
        emailValidator.setDisposableEmailDomains(
            Arrays.asList("chacuo.net", "bccto.me", "mailinator.com")
        );
    }

    @Test
    @DisplayName("测试有效QQ邮箱格式")
    void testValidQQEmail() {
        assertTrue(emailValidator.isValidFormat("123456@qq.com"));
        assertTrue(emailValidator.isValidFormat("987654321@qq.com"));
    }

    @Test
    @DisplayName("测试有效常规邮箱格式")
    void testValidRegularEmail() {
        assertTrue(emailValidator.isValidFormat("test@gmail.com"));
        assertTrue(emailValidator.isValidFormat("user.name@example.com"));
        assertTrue(emailValidator.isValidFormat("user+tag@domain.co.uk"));
    }

    @ParameterizedTest
    @ValueSource(strings = {
        "",
        "invalid",
        "@qq.com",
        "test@",
        "test@@qq.com",
        "test..name@qq.com",
        "test name@qq.com"
    })
    @DisplayName("测试无效邮箱格式")
    void testInvalidEmailFormat(String email) {
        assertFalse(emailValidator.isValidFormat(email));
    }

    @Test
    @DisplayName("测试临时邮箱检测")
    void testDisposableEmail() {
        assertTrue(emailValidator.isDisposableEmail("test@chacuo.net"));
        assertTrue(emailValidator.isDisposableEmail("test@bccto.me"));
        assertTrue(emailValidator.isDisposableEmail("test@mailinator.com"));
        assertFalse(emailValidator.isDisposableEmail("test@qq.com"));
    }

    @Test
    @DisplayName("测试临时邮箱抛出异常")
    void testValidateThrowsExceptionForDisposableEmail() {
        BusinessException exception = assertThrows(
            BusinessException.class,
            () -> emailValidator.validate("test@chacuo.net")
        );
        assertEquals("不支持临时邮箱，请使用常用邮箱", exception.getMessage());
    }

    @Test
    @DisplayName("测试无效格式抛出异常")
    void testValidateThrowsExceptionForInvalidFormat() {
        BusinessException exception = assertThrows(
            BusinessException.class,
            () -> emailValidator.validate("invalid-email")
        );
        assertEquals("邮箱格式不正确", exception.getMessage());
    }

    @Test
    @DisplayName("测试正常邮箱通过验证")
    void testValidateSuccess() {
        assertDoesNotThrow(() -> emailValidator.validate("test@qq.com"));
    }

    @Test
    @DisplayName("测试null邮箱")
    void testNullEmail() {
        assertFalse(emailValidator.isValidFormat(null));
        assertTrue(emailValidator.isDisposableEmail(null));
    }
}