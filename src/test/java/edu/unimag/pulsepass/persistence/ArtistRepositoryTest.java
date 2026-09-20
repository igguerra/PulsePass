package edu.unimag.pulsepass.persistence;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.transaction.annotation.Transactional;

import edu.unimag.pulsepass.persistence.domain.Artist;
import edu.unimag.pulsepass.persistence.repository.ArtistRepository;

@SpringBootTest
@Import(TestcontainersConfiguration.class)
@Transactional
class ArtistRepositoryTest {

    @Autowired
    ArtistRepository artistRepository;

    @Test
    void persistsAndRetrievesArtist() { // FR-ART-001
        Artist saved = artistRepository.saveAndFlush(
                new Artist("Test Artist Uno", "Colombia", "Salsa"));

        Artist found = artistRepository.findById(saved.getId()).orElseThrow();

        assertThat(found.getStageName()).isEqualTo("Test Artist Uno");
        assertThat(found.getCountry()).isEqualTo("Colombia");
        assertThat(found.isActive()).isTrue();
    }

    @Test
    void rejectsDuplicateStageName() { // FR-ART-002
        artistRepository.saveAndFlush(new Artist("Test Artist Dup", null, null));

        assertThatThrownBy(() ->
                artistRepository.saveAndFlush(new Artist("Test Artist Dup", null, null)))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void rejectsStageNameAlreadySeededByV2() { // V2 + FR-ART-002
        assertThat(artistRepository.findByStageName("Solar Beat")).isPresent();

        assertThatThrownBy(() ->
                artistRepository.saveAndFlush(new Artist("Solar Beat", null, null)))
                .isInstanceOf(DataIntegrityViolationException.class);
    }
}