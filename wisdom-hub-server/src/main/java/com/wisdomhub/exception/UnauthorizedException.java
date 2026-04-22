package com.wisdomhub.exception;

/**
 * 未授权异常（Token非法/失效）
 */
public class UnauthorizedException extends RuntimeException {
    
    private final Integer code = 401;
    
    public UnauthorizedException(String message) {
        super(message);
    }
    
    public Integer getCode() {
        return code;
    }
}