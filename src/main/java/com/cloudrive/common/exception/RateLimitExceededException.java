package com.cloudrive.common.exception;

/**
 * @author cd
 * @date 2025/10/10
 * @description
 */
public class RateLimitExceededException extends RuntimeException {
    public RateLimitExceededException(String message) {
        super(message);
    }
}
