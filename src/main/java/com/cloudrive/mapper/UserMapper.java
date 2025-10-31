package com.cloudrive.mapper;

import com.cloudrive.model.entity.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * @author cd
 * @date 2025/10/10
 * @description
 */

@Mapper
public interface UserMapper {
    User findUserByEmail(@Param("email") String email);
    User findUserById(@Param("userId") String userId);

    void save(User user);
}
