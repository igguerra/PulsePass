package edu.unimag.pulsepass.persistence;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.transaction.annotation.Transactional;

import edu.unimag.pulsepass.persistence.domain.User;
import edu.unimag.pulsepass.persistence.repository.UserRepository;

@SpringBootTest
@Import(TestcontainersConfiguration.class)
@Transactional
class UserRepositoryTest {

    @Autowired
    UserRepository userRepository;

    private User user(String username, String email) {
        return new User(username, email);
    }

    @Test
    void persistsAndRetrievesById() { // FR-USR-001
        User saved = userRepository.saveAndFlush(user("andrea", "andrea@pulsepass.com"));

        User found = userRepository.findById(saved.getId()).orElseThrow();
        assertThat(found.getUsername()).isEqualTo("andrea");
        assertThat(found.getEmail()).isEqualTo("andrea@pulsepass.com");
        assertThat(found.isActive()).isTrue();
    }

    @Test
    void findsByEmailIgnoringCase() { // Query Method: búsqueda por email ignorando mayúsculas
        userRepository.saveAndFlush(user("carlos", "Carlos@PulsePass.com"));

        User found = userRepository.findByEmailIgnoreCase("carlos@pulsepass.com").orElseThrow();

        assertThat(found.getUsername()).isEqualTo("carlos");
    }

    @Test
    void rejectsDuplicateUsername() { // FR-USR-002
        userRepository.saveAndFlush(user("laura", "laura@pulsepass.com"));

        assertThatThrownBy(() ->
                userRepository.saveAndFlush(user("laura", "otro@pulsepass.com")))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void rejectsDuplicateEmail() { // FR-USR-002
        userRepository.saveAndFlush(user("miguel", "miguel@pulsepass.com"));

        assertThatThrownBy(() ->
                userRepository.saveAndFlush(user("otro_usuario", "miguel@pulsepass.com")))
                .isInstanceOf(DataIntegrityViolationException.class);
    }
}
