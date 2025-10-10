package com.cloudrive.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import com.cloudrive.common.enums.ErrorCode;
import com.cloudrive.common.util.EmailUtil;
import com.cloudrive.common.util.ExceptionUtil;
import com.cloudrive.common.util.PasswordUtil;
import com.cloudrive.mapper.UserMapper;
import com.cloudrive.model.dto.LoginDTO;
import com.cloudrive.model.dto.RegisterDTO;
import com.cloudrive.model.entity.User;
import com.cloudrive.redis.VerificationCodeRedis;
import com.cloudrive.service.UserService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.Random;
import java.util.UUID;

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

    @Override
    @Transactional
    public void register(RegisterDTO registerDTO) {
        User user = userMapper.findUserByEmail(registerDTO.getEmail());
        // 检查邮箱是否已被注册
        ExceptionUtil.throwIf(
                Optional.ofNullable(user).isPresent(),
                ErrorCode.EMAIL_ALREADY_EXIST
        );
        // 验证验证码
        String savedCode = verificationCodeRedis.getVerificationCode(registerDTO.getEmail());
        ExceptionUtil.throwIf(
                savedCode == null || !savedCode.equals(registerDTO.getCode()),
                ErrorCode.VERIFICATION_CODE_ERROR
        );
        user = new User();
        // 简单的时间戳+随机数实现
        String userId = UUID.randomUUID().toString().replace("-", "");
        user.setUserId(userId);
        user.setUsername(registerDTO.getUsername());
        user.setPassword(PasswordUtil.encode(registerDTO.getPassword()));
        user.setEmail(registerDTO.getEmail());
        user.setStatus(1); // 1表示正常状态

        userMapper.save(user);

        // 删除验证码
        verificationCodeRedis.deleteVerificationCode(registerDTO.getEmail());
    }

    @Override
    public String login(LoginDTO loginDTO) {
        logger.info("开始处理登录请求，用户名：{}", loginDTO.getEmail());
        User userByEmail = userMapper.findUserByEmail(loginDTO.getEmail());
        Optional<User> userOpt = Optional.ofNullable(userByEmail);
        if (userOpt.isEmpty()) {
            logger.warn("登录失败：邮箱：{} 不存在", loginDTO.getEmail());
            ExceptionUtil.throwBizException(ErrorCode.USER_NOT_FOUND);
        }

        User user = userOpt.get();
        if (!PasswordUtil.matches(loginDTO.getPassword(), user.getPassword())) {
            logger.warn("登录失败：密码错误，邮箱：{}", loginDTO.getEmail());
            ExceptionUtil.throwBizException(ErrorCode.INVALID_PASSWORD);
        }

        if (user.getStatus() != 1) {
            logger.warn("登录失败：账号已被禁用，邮箱：{}", loginDTO.getEmail());
            ExceptionUtil.throwBizException(ErrorCode.ACCOUNT_DISABLED);
        }

        // 登录
        StpUtil.login(user.getUserId());

        // 返回token
        String token = StpUtil.getTokenValue();
        logger.info("登录成功，邮箱：{}，token：{}", loginDTO.getEmail(), token);
        return token;
    }

    @Override
    public User findUserByEmail(String email) {
        return userMapper.findUserByEmail(email);
    }

    @Override
    public User findUserById(String userId) {
        return userMapper.findUserById(userId);
    }

    @Override
    public void logout() {
        ExceptionUtil.throwIf(!StpUtil.isLogin(), ErrorCode.USER_NOT_LOGGED_IN);
        StpUtil.logout();
    }

    @Override
    public void forceLogout(Long userId) {
        ExceptionUtil.throwIf(!StpUtil.isLogin(), ErrorCode.USER_NOT_LOGGED_IN, "操作者未登录");
        // 强制指定用户下线
        StpUtil.logout(userId);
    }
}
