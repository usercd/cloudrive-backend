package com.cloudrive.service;


import com.cloudrive.model.dto.LoginDTO;
import com.cloudrive.model.dto.RegisterDTO;
import com.cloudrive.model.entity.User;
import jakarta.validation.Valid;

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

    /**
     * 用户注册
     */
    void register(RegisterDTO registerDTO);

    /**
     * 用户登录
     */
    String login(LoginDTO loginDTO);

    User findUserByEmail(String email);
    User findUserById(String userId);
}
