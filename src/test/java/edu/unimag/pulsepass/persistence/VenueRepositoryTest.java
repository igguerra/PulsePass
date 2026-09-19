package edu.unimag.pulsepass.persistence;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.transaction.annotation.Transactional;

import edu.unimag.pulsepass.persistence.domain.Venue;
import edu.unimag.pulsepass.persistence.repository.VenueRepository;

@SpringBootTest
@Import(TestcontainersConfiguration.class)
@Transactional
class VenueRepositoryTest {

    @Autowired
    VenueRepository venueRepository;

    private Venue venue(String code, int capacity) {
        return new Venue(code, "Marina Convention Center", "Santa Marta",
                "Carrera 1 # 1-1", capacity);
    }

    @Test
    void persistsAndRetrievesByIdAndCode() { // FR-VEN-001, AC-001
        Venue saved = venueRepository.saveAndFlush(venue("VEN-SMR-01", 5000));

        assertThat(venueRepository.findById(saved.getId())).isPresent();

        Venue found = venueRepository.findByCode("VEN-SMR-01").orElseThrow();
        assertThat(found.getCapacity()).isGreaterThan(0);
        assertThat(found.isActive()).isTrue();
    }

    @Test
    void rejectsDuplicateCode() { // FR-VEN-002
        venueRepository.saveAndFlush(venue("VEN-DUP", 100));

        assertThatThrownBy(() -> venueRepository.saveAndFlush(venue("VEN-DUP", 200)))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void rejectsZeroCapacity() { // FR-VEN-003
        assertThatThrownBy(() -> venueRepository.saveAndFlush(venue("VEN-ZERO", 0)))
                .isInstanceOf(DataIntegrityViolationException.class);
    }
}
