package com.rentivo.rental_saas.auth.mappers;

import com.rentivo.rental_saas.auth.dto.CreateUserRequest;
import com.rentivo.rental_saas.auth.dto.UserResponse;
import com.rentivo.rental_saas.auth.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface UserMapper {
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "passwordHash", ignore = true)
    @Mapping(target = "enabled", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    User toEntity(CreateUserRequest createUserRequest);
    UserResponse toResponse(User user);
}
