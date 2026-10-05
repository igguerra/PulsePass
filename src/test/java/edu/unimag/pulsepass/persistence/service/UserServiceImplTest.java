package edu.unimag.pulsepass.persistence.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

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
import edu.unimag.pulsepass.persistence.service.impl.UserServiceImpl;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    private static final String USERNAME = "andrea";
    private static final String EMAIL = "andrea@email.com";
    private static final LocalDate BIRTH_DATE = LocalDate.of(2001, 5, 10);

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserProfileRepository userProfileRepository;

    @Mock
    private UserMapper userMapper;

    @InjectMocks
    private UserServiceImpl userService;

    // ---------- helpers ----------

    private RegisterUserRequest request(LocalDate birthDate) {
        return new RegisterUserRequest(USERNAME, EMAIL, "Andrea", "Gómez",
                "3001234567", "Santa Marta", birthDate);
    }

    private UserResponse response() {
        return new UserResponse(1L, USERNAME, EMAIL, true, "Andrea", "Gómez",
                "3001234567", "Santa Marta", BIRTH_DATE);
    }

    // ---------- register ----------

    @Test
    void register_validRequest_savesUserAndProfile() { // TEST-USER-001
        // ARRANGE
        UserResponse expected = response();
        when(userRepository.existsByUsername(USERNAME)).thenReturn(false);
        when(userRepository.existsByEmailIgnoreCase(EMAIL)).thenReturn(false);
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(userProfileRepository.save(any(UserProfile.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        when(userMapper.toResponse(any(User.class), any(UserProfile.class))).thenReturn(expected);

        // ACT
        UserResponse result = userService.register(request(BIRTH_DATE));

        // ASSERT
        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        ArgumentCaptor<UserProfile> profileCaptor = ArgumentCaptor.forClass(UserProfile.class);
        verify(userRepository).save(userCaptor.capture());
        verify(userProfileRepository).save(profileCaptor.capture());

        User savedUser = userCaptor.getValue();
        UserProfile savedProfile = profileCaptor.getValue();
        assertThat(savedUser.getUsername()).isEqualTo(USERNAME);
        assertThat(savedUser.getEmail()).isEqualTo(EMAIL);
        assertThat(savedUser.isActive()).isTrue();
        assertThat(savedProfile.getUser()).isSameAs(savedUser);
        assertThat(savedProfile.getFirstName()).isEqualTo("Andrea");
        assertThat(savedProfile.getBirthDate()).isEqualTo(BIRTH_DATE);
        assertThat(result).isEqualTo(expected);
    }

    @Test
    void register_duplicateUsername_throwsDuplicateResource() { // TEST-USER-002
        // ARRANGE
        when(userRepository.existsByUsername(USERNAME)).thenReturn(true);

        // ACT + ASSERT
        assertThatThrownBy(() -> userService.register(request(BIRTH_DATE)))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessageContaining("Username");
        verify(userRepository, never()).save(any(User.class));
        verify(userProfileRepository, never()).save(any(UserProfile.class));
    }

    @Test
    void register_duplicateEmail_throwsDuplicateResource() { // TEST-USER-003
        // ARRANGE
        when(userRepository.existsByUsername(USERNAME)).thenReturn(false);
        when(userRepository.existsByEmailIgnoreCase(EMAIL)).thenReturn(true);

        // ACT + ASSERT
        assertThatThrownBy(() -> userService.register(request(BIRTH_DATE)))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessageContaining("Email");
        verify(userRepository, never()).save(any(User.class));
        verify(userProfileRepository, never()).save(any(UserProfile.class));
    }

    @Test
    void register_futureBirthDate_throwsBusinessRule() { // TEST-USER-004
        // ARRANGE
        LocalDate tomorrow = LocalDate.now().plusDays(1);

        // ACT + ASSERT
        assertThatThrownBy(() -> userService.register(request(tomorrow)))
                .isInstanceOf(BusinessRuleException.class);
        verify(userRepository, never()).save(any(User.class));
        verify(userProfileRepository, never()).save(any(UserProfile.class));
    }

    // ---------- consultas ----------

    @Test
    void findByEmail_existingUser_returnsDto() { // FR-SVC-011
        // ARRANGE
        User user = new User(USERNAME, EMAIL);
        UserProfile profile = new UserProfile("Andrea", "Gómez", user);
        UserResponse expected = response();
        when(userRepository.findByEmailIgnoreCase(EMAIL)).thenReturn(Optional.of(user));
        when(userProfileRepository.findByUserId(any())).thenReturn(Optional.of(profile));
        when(userMapper.toResponse(user, profile)).thenReturn(expected);

        // ACT
        UserResponse result = userService.findByEmail(EMAIL);

        // ASSERT
        assertThat(result).isEqualTo(expected);
    }

    @Test
    void findByUsername_missingUser_throwsResourceNotFound() { // FR-SVC-012
        // ARRANGE
        when(userRepository.findByUsername(USERNAME)).thenReturn(Optional.empty());

        // ACT + ASSERT
        assertThatThrownBy(() -> userService.findByUsername(USERNAME))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining(USERNAME);
        verify(userMapper, never()).toResponse(any(), any());
    }

}
