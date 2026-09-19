package edu.unimag.pulsepass.persistence;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;

@SpringBootTest
@Import(TestcontainersConfiguration.class)
class FlywayMigrationTest {

    @Autowired
    JdbcTemplate jdbcTemplate;

    @Test
    void appliesV1V2V3FromEmptyDatabase() {
        Integer applied = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM flyway_schema_history WHERE success = TRUE",
                Integer.class);
        assertThat(applied).isEqualTo(3);
    }

    @Test
    void seedsInitialArtists() {
        Integer artists = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM artists", Integer.class);
        assertThat(artists).isEqualTo(5);
    }

    @Test
    void streamingUrlColumnExistsAndIsNullable() {
        String nullable = jdbcTemplate.queryForObject(
                """
                SELECT is_nullable FROM information_schema.columns
                WHERE table_name = 'events' AND column_name = 'streaming_url'
                """, String.class);
        assertThat(nullable).isEqualTo("YES");
    }
}