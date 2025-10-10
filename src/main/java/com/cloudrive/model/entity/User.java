package com.cloudrive.model.entity;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * @author cd
 * @date 2025/10/10
 * @description
 */
@Data
public class User {
    private String userId;

    private String avatarUrl;

    private String username;

    private String password;

    private String email;

    private Integer status;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
