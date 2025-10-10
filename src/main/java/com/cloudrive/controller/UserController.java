package com.cloudrive.controller;

import com.cloudrive.common.Result;
import com.cloudrive.model.dto.EmailSendDTO;
import com.cloudrive.model.dto.LoginDTO;
import com.cloudrive.model.dto.RegisterDTO;
import com.cloudrive.model.entity.User;
import com.cloudrive.model.vo.UserLoginVO;
import com.cloudrive.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author cd
 * @date 2025/10/10
 * @description
 */

@RestController
@Validated
@RequiredArgsConstructor
@RequestMapping("/api/user")
@Slf4j
public class UserController {

    private final UserService userService;

    /**
     * 发送验证码
     */
    @PostMapping("/verification-code")
    public Result<Void> sendVerificationCode(@RequestBody EmailSendDTO emailSendDTO) {
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
        String token = userService.login(loginDTO);
        User user = userService.findUserByEmail(loginDTO.getEmail());
        UserLoginVO userLoginVO = new UserLoginVO();
        userLoginVO.setUserId(user.getUserId());
        userLoginVO.setToken(token);
        return Result.success(userLoginVO);
    }
}
