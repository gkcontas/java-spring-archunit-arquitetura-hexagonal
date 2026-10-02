package com.gkcontas.hexagonal.adapter.out.persistence.memory;

import com.gkcontas.hexagonal.application.port.out.ReservationRepository;
import com.gkcontas.hexagonal.domain.model.Reservation;
import com.gkcontas.hexagonal.domain.model.ReservationId;
import com.gkcontas.hexagonal.domain.model.RoomId;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * The second outbound adapter, and the one that proves the first was not load-bearing.
 *
 * <p>It is not a mock. It implements the same port with real behaviour, which is why the
 * use case tests need no mocking framework at all: a fake that actually stores and queries
 * tells you more than a stub that returns whatever the test wanted to hear.
 *
 * <p>It also runs the same contract test suite as the JPA adapter. Two implementations
 * that pass the same tests are interchangeable in the sense that matters — not because a
 * diagram says so.
 */
public class InMemoryReservationRepository implements ReservationRepository {

    private final Map<ReservationId, Reservation> reservations = new ConcurrentHashMap<>();

    @Override
    public Reservation save(Reservation reservation) {
        reservations.put(reservation.id(), reservation);
        return reservation;
    }

    @Override
    public Optional<Reservation> findById(ReservationId id) {
        return Optional.ofNullable(reservations.get(id));
    }

    @Override
    public List<Reservation> findByRoomBetween(RoomId roomId, Instant from, Instant to) {
        return reservations.values().stream()
                .filter(reservation -> reservation.roomId().equals(roomId))
                // Same half-open comparison the SQL adapter uses. The contract test exists
                // precisely so these two never drift apart unnoticed.
                .filter(reservation -> reservation.slot().start().isBefore(to)
                        && from.isBefore(reservation.slot().end()))
                .toList();
    }

    public void clear() {
        reservations.clear();
    }
}
