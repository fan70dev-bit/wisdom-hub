package com.wisdomhub.util;

import com.wisdomhub.exception.BusinessException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.regex.Pattern;

/**
 * 邮箱验证工具类
 */
@Component
public class EmailValidator {

    @Value("#{'${app.security.disposable-email-domains}'.split(',')}")
    private List<String> disposableEmailDomains;

    private static final Pattern EMAIL_PATTERN = Pattern.compile(
            "^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@" +
                    "(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,7}$"
    );

    /**
     * 验证邮箱格式
     */
    public boolean isValidFormat(String email) {
        if (email == null || email.trim().isEmpty()) {
            return false;
        }
        return EMAIL_PATTERN.matcher(email).matches();
    }

    /**
     * 检查是否为临时邮箱
     */
    public boolean isDisposableEmail(String email) {
        if (email == null) {
            return true;
        }

        String domain = extractDomain(email);
        for (String disposableDomain : disposableEmailDomains) {
            if (disposableDomain.equalsIgnoreCase(domain)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 提取邮箱域名
     */
    private String extractDomain(String email) {
        int atIndex = email.lastIndexOf('@');
        return atIndex > 0 ? email.substring(atIndex + 1).toLowerCase() : "";
    }

    /**
     * 综合验证
     */
    public void validate(String email) {
        if (!isValidFormat(email)) {
            throw new BusinessException("邮箱格式不正确");
        }

        if (isDisposableEmail(email)) {
            throw new BusinessException("不支持临时邮箱，请使用常用邮箱");
        }
    }

    // Getter/Setter for testing
    public List<String> getDisposableEmailDomains() {
        return disposableEmailDomains;
    }

    public void setDisposableEmailDomains(List<String> disposableEmailDomains) {
        this.disposableEmailDomains = disposableEmailDomains;
    }
}