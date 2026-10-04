package edu.unimag.pulsepass.persistence.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import edu.unimag.pulsepass.persistence.domain.User;
import edu.unimag.pulsepass.persistence.domain.UserProfile;
import edu.unimag.pulsepass.persistence.dto.response.UserResponse;

@Mapper(componentModel = "spring")
public interface UserMapper {

    // User y UserProfile tienen ambos un "id": se indica explícitamente que el del response es el del User.
    @Mapping(target = "id", source = "user.id")
    UserResponse toResponse(User user, UserProfile profile);

}
