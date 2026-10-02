package com.gkcontas.hexagonal.adapter.out.persistence;

import com.gkcontas.hexagonal.adapter.out.persistence.memory.InMemoryReservationRepository;
import com.gkcontas.hexagonal.application.port.out.ReservationRepository;
import org.junit.jupiter.api.BeforeEach;

/**
 * The contract against the in-memory adapter: no Spring, no Docker, milliseconds.
 */
class InMemoryReservationRepositoryTest extends ReservationRepositoryContract {

    private InMemoryReservationRepository repository;

    @BeforeEach
    void setUp() {
        repository = new InMemoryReservationRepository();
    }

    @Override
    protected ReservationRepository repository() {
        return repository;
    }
}
