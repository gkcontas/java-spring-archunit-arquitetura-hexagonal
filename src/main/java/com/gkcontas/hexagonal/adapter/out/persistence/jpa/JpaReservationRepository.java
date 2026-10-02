package com.gkcontas.hexagonal.adapter.out.persistence.jpa;

import com.gkcontas.hexagonal.application.port.out.ReservationRepository;
import com.gkcontas.hexagonal.domain.model.Reservation;
import com.gkcontas.hexagonal.domain.model.ReservationId;
import com.gkcontas.hexagonal.domain.model.RoomId;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

/**
 * The adapter: it implements the port and translates in both directions.
 *
 * <p>Thin on purpose. Everything it does is call Spring Data and map — no decisions, no
 * rules. When this class starts growing conditionals, the business logic is leaking into
 * the infrastructure.
 */
public class JpaReservationRepository implements ReservationRepository {

    private final SpringDataReservationRepository repository;

    JpaReservationRepository(SpringDataReservationRepository repository) {
        this.repository = repository;
    }

    @Override
    public Reservation save(Reservation reservation) {
        repository.save(PersistenceMapper.toEntity(reservation));
        return reservation;
    }

    @Override
    public Optional<Reservation> findById(ReservationId id) {
        return repository.findById(id.value()).map(PersistenceMapper::toDomain);
    }

    @Override
    public List<Reservation> findByRoomBetween(RoomId roomId, Instant from, Instant to) {
        return repository.findOverlapping(roomId.value(), from, to).stream()
                .map(PersistenceMapper::toDomain)
                .toList();
    }
}
