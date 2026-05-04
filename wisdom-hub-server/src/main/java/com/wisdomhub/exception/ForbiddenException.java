package com.wisdomhub.exception;

/**
 * 禁止访问异常（403）
 */
public class ForbiddenException extends RuntimeException {
    
    private final Integer code = 403;
    
    public ForbiddenException(String message) {
        super(message);
    }
    
    public Integer getCode() {
        return code;
    }
}