package com.cloudrive.service.impl;

import com.cloudrive.common.enums.ErrorCode;
import com.cloudrive.common.util.EmailUtil;
import com.cloudrive.common.util.ExceptionUtil;
import com.cloudrive.mapper.UserMapper;
import com.cloudrive.model.entity.User;
import com.cloudrive.redis.VerificationCodeRedis;
import com.cloudrive.service.UserService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.Random;

/**
 * @author cd
 * @date 2025/10/10
 * @description
 */

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private static final Logger logger = LoggerFactory.getLogger(UserServiceImpl.class);

    private final VerificationCodeRedis verificationCodeRedis;
    private final UserMapper userMapper;
    @Override
    public void sendVerificationCode(String email) {
        User user = userMapper.findUserByEmail(email);
        // 检查邮箱是否已被注册
        ExceptionUtil.throwIf(
                Optional.ofNullable(user).isPresent(),
                ErrorCode.EMAIL_ALREADY_EXIST
        );

        // 生成6位数字验证码
        String code = String.valueOf(100000 + new Random().nextInt(900000));

        try {
            // 使用EmailUtils工具类发送验证码邮件
            boolean success = EmailUtil.sendVerificationCode(email, code);
            if (!success) {
                throw new Exception("邮件发送失败");
            }

            // 保存验证码到Redis
            verificationCodeRedis.saveVerificationCode(email, code);
        } catch (Exception e) {
            logger.error("发送验证码失败：{}", e.getMessage());
            ExceptionUtil.throwBizException(ErrorCode.EMAIL_SEND_ERROR);
        }
    }
}
