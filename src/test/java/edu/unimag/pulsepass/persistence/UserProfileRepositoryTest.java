package edu.unimag.pulsepass.persistence;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDate;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.transaction.annotation.Transactional;

import edu.unimag.pulsepass.persistence.domain.User;
import edu.unimag.pulsepass.persistence.domain.UserProfile;
import edu.unimag.pulsepass.persistence.repository.UserProfileRepository;
import edu.unimag.pulsepass.persistence.repository.UserRepository;

@SpringBootTest
@Import(TestcontainersConfiguration.class)
@Transactional
class UserProfileRepositoryTest {

    @Autowired
    UserRepository userRepository;

    @Autowired
    UserProfileRepository userProfileRepository;

    private User savedUser(String username, String email) {
        return userRepository.saveAndFlush(new User(username, email));
    }

    @Test
    void retrievesProfileDataFromTheUser() { // FR-USR-004
        User user = savedUser("andrea", "andrea@pulsepass.com");

        UserProfile profile = new UserProfile("Andrea", "Ramirez", user);
        profile.setPhone("3000000000");
        profile.setCity("Santa Marta");
        profile.setBirthDate(LocalDate.of(1998, 5, 20));
        userProfileRepository.saveAndFlush(profile);

        UserProfile found = userProfileRepository.findByUserId(user.getId()).orElseThrow();

        assertThat(found.getFirstName()).isEqualTo("Andrea");
        assertThat(found.getLastName()).isEqualTo("Ramirez");
        assertThat(found.getPhone()).isEqualTo("3000000000");
        assertThat(found.getCity()).isEqualTo("Santa Marta");
        assertThat(found.getBirthDate()).isEqualTo(LocalDate.of(1998, 5, 20));
        assertThat(found.getUser().getEmail()).isEqualTo("andrea@pulsepass.com");
    }

    @Test
    void rejectsSecondProfileForTheSameUser() { // FR-USR-003, AC-004
        User user = savedUser("carlos", "carlos@pulsepass.com");
        userProfileRepository.saveAndFlush(new UserProfile("Carlos", "Perez", user));

        assertThatThrownBy(() ->
                userProfileRepository.saveAndFlush(new UserProfile("Carlos2", "Perez2", user)))
                .isInstanceOf(DataIntegrityViolationException.class);
    }
}
