package edu.unimag.pulsepass.persistence.service;

import edu.unimag.pulsepass.persistence.dto.request.RegisterUserRequest;
import edu.unimag.pulsepass.persistence.dto.response.UserResponse;

public interface UserService {

    UserResponse register(RegisterUserRequest request);

    UserResponse findByEmail(String email);

    UserResponse findByUsername(String username);

}
