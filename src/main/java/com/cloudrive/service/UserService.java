package com.cloudrive.service;


/**
 * @author cd
 * @date 2025/10/10
 * @description
 */
public interface UserService {
    /**
     * 发送验证码
     */
    void sendVerificationCode(String email);
}
