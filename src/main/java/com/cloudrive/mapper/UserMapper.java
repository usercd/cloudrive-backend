package com.cloudrive.mapper;

import com.cloudrive.model.entity.User;
import org.apache.ibatis.annotations.Mapper;

/**
 * @author cd
 * @date 2025/10/10
 * @description
 */

@Mapper
public interface UserMapper {
    User findUserByEmail(String email);
    User findUserById(String userId);

    void save(User user);
}
