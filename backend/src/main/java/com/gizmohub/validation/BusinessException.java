package com.gizmohub.validation;

import org.springframework.http.HttpStatus;

/**
 * Thrown whenever a business rule is broken.
 * "code" is the requirement / rule ID (e.g. "CART-02") so every error response
 * can be traced straight back to the requirements document.
 * GlobalExceptionHandler turns it into JSON: {"status":409,"code":"CART-02","message":"..."}
 */
public class BusinessException extends RuntimeException {

    private final HttpStatus status;
    private final String code;

    public BusinessException(HttpStatus status, String code, String message) {
        super(message);
        this.status = status;
        this.code = code;
    }

    public static BusinessException badRequest(String code, String message) {
        return new BusinessException(HttpStatus.BAD_REQUEST, code, message);
    }

    public static BusinessException notFound(String code, String message) {
        return new BusinessException(HttpStatus.NOT_FOUND, code, message);
    }

    public static BusinessException conflict(String code, String message) {
        return new BusinessException(HttpStatus.CONFLICT, code, message);
    }

    public HttpStatus getStatus() { return status; }
    public String getCode() { return code; }
}
