package edu.unimag.pulsepass.persistence.service.impl;

import java.time.LocalDate;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import edu.unimag.pulsepass.persistence.domain.User;
import edu.unimag.pulsepass.persistence.domain.UserProfile;
import edu.unimag.pulsepass.persistence.dto.request.RegisterUserRequest;
import edu.unimag.pulsepass.persistence.dto.response.UserResponse;
import edu.unimag.pulsepass.persistence.exception.BusinessRuleException;
import edu.unimag.pulsepass.persistence.exception.DuplicateResourceException;
import edu.unimag.pulsepass.persistence.exception.ResourceNotFoundException;
import edu.unimag.pulsepass.persistence.mapper.UserMapper;
import edu.unimag.pulsepass.persistence.repository.UserProfileRepository;
import edu.unimag.pulsepass.persistence.repository.UserRepository;
import edu.unimag.pulsepass.persistence.service.UserService;

@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final UserProfileRepository userProfileRepository;
    private final UserMapper userMapper;

    public UserServiceImpl(
            UserRepository userRepository,
            UserProfileRepository userProfileRepository,
            UserMapper userMapper) {
        this.userRepository = userRepository;
        this.userProfileRepository = userProfileRepository;
        this.userMapper = userMapper;
    }

    // BR-USER-004: User y UserProfile se crean en la misma transacción
    @Override
    @Transactional
    public UserResponse register(RegisterUserRequest request) {
        // BR-USER-005: birthDate no puede ser futura
        if (request.birthDate() != null && request.birthDate().isAfter(LocalDate.now())) {
            throw new BusinessRuleException("Birth date cannot be in the future.");
        }

        // BR-USER-001: username único
        if (userRepository.existsByUsername(request.username())) {
            throw new DuplicateResourceException("Username already exists.");
        }

        // BR-USER-002: email único ignorando mayúsculas/minúsculas
        if (userRepository.existsByEmailIgnoreCase(request.email())) {
            throw new DuplicateResourceException("Email already exists.");
        }

        // BR-USER-003: el usuario inicia con active = true (valor por defecto de la entidad)
        User user = userRepository.save(new User(request.username(), request.email()));

        UserProfile profile = new UserProfile(request.firstName(), request.lastName(), user);
        profile.setPhone(request.phone());
        profile.setCity(request.city());
        profile.setBirthDate(request.birthDate());
        UserProfile savedProfile = userProfileRepository.save(profile);

        return userMapper.toResponse(user, savedProfile);
    }

    @Override
    @Transactional(readOnly = true)
    public UserResponse findByEmail(String email) {
        User user = userRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + email));
        return toResponse(user);
    }

    @Override
    @Transactional(readOnly = true)
    public UserResponse findByUsername(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + username));
        return toResponse(user);
    }

    // User no tiene referencia a su perfil: se busca por el id del usuario.
    private UserResponse toResponse(User user) {
        UserProfile profile = userProfileRepository.findByUserId(user.getId()).orElse(null);
        return userMapper.toResponse(user, profile);
    }

}
