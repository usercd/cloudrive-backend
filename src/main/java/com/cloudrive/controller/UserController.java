package com.cloudrive.controller;

import com.cloudrive.common.Result;
import com.cloudrive.model.dto.EmailSendDTO;
import com.cloudrive.service.UserService;
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
}
