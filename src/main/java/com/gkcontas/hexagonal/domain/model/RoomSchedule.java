package com.gkcontas.hexagonal.domain.model;

import com.gkcontas.hexagonal.domain.exception.InvalidTimeSlotException;
import com.gkcontas.hexagonal.domain.exception.RoomCapacityExceededException;
import com.gkcontas.hexagonal.domain.exception.RoomUnavailableException;
import java.time.Instant;
import java.util.List;
import java.util.Objects;

/**
 * The room together with what is already booked in it — which is the unit the booking rule
 * actually needs.
 *
 * <p>This is where the hexagon earns its keep. "Does this slot conflict with another?" is
 * a business question, not a database one, so it is answered here, in plain Java, with no
 * idea that a database exists. The application layer's only job is to hand over the
 * existing reservations; swapping PostgreSQL for a map changes nothing about this class.
 *
 * <p>The tempting alternative is a {@code SELECT ... WHERE overlaps} that answers the
 * question in SQL. It is faster, and it moves the most important rule of the system into a
 * string that no unit test covers and that the second adapter has to reimplement.
 */
public record RoomSchedule(Room room, List<Reservation> reservations) {

    public RoomSchedule {
        Objects.requireNonNull(room, "room must not be null");
        reservations = List.copyOf(Objects.requireNonNull(reservations, "reservations must not be null"));
    }

    public Reservation book(String organizer, TimeSlot slot, int attendees, Instant now) {
        if (slot.startsBefore(now)) {
            throw new InvalidTimeSlotException("Cannot book a slot that starts in the past");
        }
        if (!room.holds(attendees)) {
            throw new RoomCapacityExceededException(attendees, room.capacity());
        }
        if (isBusyDuring(slot)) {
            throw new RoomUnavailableException(room.id().value(), slot.toString());
        }
        return Reservation.confirm(room.id(), organizer, slot, attendees);
    }

    public boolean isBusyDuring(TimeSlot slot) {
        return reservations.stream()
                .filter(Reservation::isConfirmed)
                .anyMatch(reservation -> reservation.slot().overlaps(slot));
    }
}
