package com.cloudrive.controller;

import com.cloudrive.common.Result;
import com.cloudrive.model.dto.EmailSendDTO;
import com.cloudrive.model.dto.LoginDTO;
import com.cloudrive.model.dto.RegisterDTO;
import com.cloudrive.model.entity.User;
import com.cloudrive.model.vo.UserLoginVO;
import com.cloudrive.model.vo.UserVO;
import com.cloudrive.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * @author cd
 * @date 2025/10/10
 * @description
 */

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/user")
@Slf4j
public class UserController {

    private final UserService userService;

    /**
     * 发送验证码
     */
    @PostMapping("/verification-code")
    public Result<Void> sendVerificationCode(@Valid @RequestBody EmailSendDTO emailSendDTO) {
        userService.sendVerificationCode(emailSendDTO.getEmail());
        return Result.success();
    }

    /**
     * 注册
     */
    @PostMapping("/register")
    public Result<Void> register(@Valid @RequestBody RegisterDTO registerDTO) {
        userService.register(registerDTO);
        return Result.success();
    }

    /**
     * 登录
     */
    @PostMapping("/login")
    public Result<UserLoginVO> login(@Valid @RequestBody LoginDTO loginDTO) {
        UserLoginVO userLoginVO = userService.loginWithPassword(loginDTO);
        return Result.success(userLoginVO);
    }

    /**
     * 通过用户ID查询用户信息
     */
    @GetMapping("{userId}")
    public Result<UserVO> getUserInfo(@PathVariable("userId") String userId) {
        UserVO userVO = userService.findUserById(userId);
        if (userVO == null) {
            return Result.error("用户不存在！");
        }
        return Result.success(userVO);
    }


    /**
     * 登出
     */
    @PostMapping("/logout")
    public Result<Void> logout() {
        userService.logout();
        return Result.success();
    }

    /**
     * 强制登出
     */
    @PostMapping("/forceLogout/{userId}")
    public Result<Void> forceLogout(@PathVariable Long userId) {
        userService.forceLogout(userId);
        return Result.success();
    }
}
