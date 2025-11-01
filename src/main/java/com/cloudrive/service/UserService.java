package com.cloudrive.service;

import com.cloudrive.model.dto.LoginDTO;
import com.cloudrive.model.dto.RegisterDTO;
import com.cloudrive.model.entity.User;
import com.cloudrive.model.vo.UserLoginVO;
import com.cloudrive.model.vo.UserVO;
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

    User findUserByEmail(String email);
    UserVO findUserById(String userId);

    /**
     * 用户登出
     */
    void logout();

    /**
     * 强制下线指定用户
     */
    void forceLogout(Long userId);

    /**
     * 用户登录
     */
    UserLoginVO loginWithPassword(LoginDTO loginDTO);
}
