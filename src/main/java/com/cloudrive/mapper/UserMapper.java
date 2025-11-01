package com.cloudrive.mapper;

import com.cloudrive.model.entity.User;
import com.cloudrive.model.vo.UserVO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 * @author CD
 * @date 11/1/2025
 * @description
 */
@Mapper(componentModel = "spring")
public interface UserMapper {

    @Mapping(source = "userId", target = "userId")
    @Mapping(source = "username", target = "username")
    @Mapping(source = "email", target = "email")
    @Mapping(source = "avatarUrl", target = "avatarUrl")
    UserVO userToUserVO(User user);
}
