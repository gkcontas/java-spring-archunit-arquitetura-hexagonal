package com.gkcontas.hexagonal.application.port.out;

import com.gkcontas.hexagonal.domain.model.Reservation;
import com.gkcontas.hexagonal.domain.model.ReservationId;
import com.gkcontas.hexagonal.domain.model.RoomId;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

/**
 * An outbound port, declared by the side that <em>uses</em> it.
 *
 * <p>This is the dependency inversion that the whole arrangement rests on. The interface
 * lives in the application layer and speaks the domain's language; the adapter in
 * {@code adapter.out.persistence} implements it and deals with rows, entities and
 * dialects. The arrow points inward: persistence depends on the application, never the
 * other way round.
 *
 * <p>Note what is <em>not</em> here. No {@code Page}, no {@code Example}, no
 * {@code Specification} — those are Spring Data types, and letting one of them into this
 * signature would put the framework back in the core through the service entrance.
 */
public interface ReservationRepository {

    Reservation save(Reservation reservation);

    Optional<Reservation> findById(ReservationId id);

    /** Everything booked for the room in the window, cancelled reservations included. */
    List<Reservation> findByRoomBetween(RoomId roomId, Instant from, Instant to);
}
