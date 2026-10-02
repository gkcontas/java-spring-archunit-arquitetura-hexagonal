package com.gkcontas.hexagonal.adapter.out.persistence;

import com.gkcontas.hexagonal.application.port.out.ReservationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;

/**
 * The same contract against PostgreSQL.
 *
 * <p>The container is started once in a static initialiser and never handed to the
 * {@code @Testcontainers} extension: that extension ties a static container's lifetime to
 * the <em>test class</em>, stopping it when the class ends, while Spring caches the
 * application context and reuses it for the next class — which then points at a database
 * that no longer exists. One start per JVM lines the two lifecycles up, and Ryuk still
 * removes the container when the JVM exits.
 */
@SpringBootTest
@ActiveProfiles("jpa")
class JpaReservationRepositoryTest extends ReservationRepositoryContract {

    static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>("postgres:16-alpine")
            .withDatabaseName("booking")
            .withUsername("booking")
            .withPassword("booking");

    static {
        POSTGRES.start();
    }

    @DynamicPropertySource
    static void datasource(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", POSTGRES::getJdbcUrl);
        registry.add("spring.datasource.username", POSTGRES::getUsername);
        registry.add("spring.datasource.password", POSTGRES::getPassword);
    }

    @Autowired
    private ReservationRepository reservationRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void clearReservations() {
        jdbcTemplate.execute("DELETE FROM reservations");
    }

    @Override
    protected ReservationRepository repository() {
        return reservationRepository;
    }
}
