package com.wisdomhub.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

/**
 * 发送验证码请求
 */
public class SendCodeRequest {

    @NotBlank(message = "邮箱不能为空")
    @Email(message = "邮箱格式不正确")
    private String email;

    public SendCodeRequest() {
    }

    public SendCodeRequest(String email) {
        this.email = email;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    @Override
    public String toString() {
        return "SendCodeRequest{" +
                "email='" + email + '\'' +
                '}';
    }
}