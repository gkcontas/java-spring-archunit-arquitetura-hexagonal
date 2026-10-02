package com.gkcontas.hexagonal.domain.model;

import com.gkcontas.hexagonal.domain.exception.ReservationAlreadyCancelledException;
import com.gkcontas.hexagonal.domain.exception.ReservationAlreadyStartedException;
import java.time.Instant;
import java.util.Objects;

/**
 * A booking, and the rules about its own lifecycle.
 *
 * <p>{@code cancel} is a method here and not in a service on purpose: whether a
 * reservation may be cancelled depends only on the reservation itself, so putting the
 * decision anywhere else means every caller has to remember to ask first. Immutable, so
 * cancelling returns a new instance rather than mutating one that something else may be
 * holding.
 */
public record Reservation(
        ReservationId id,
        RoomId roomId,
        String organizer,
        TimeSlot slot,
        int attendees,
        ReservationStatus status) {

    public Reservation {
        Objects.requireNonNull(id, "id must not be null");
        Objects.requireNonNull(roomId, "room id must not be null");
        Objects.requireNonNull(slot, "slot must not be null");
        Objects.requireNonNull(status, "status must not be null");
        if (organizer == null || organizer.isBlank()) {
            throw new IllegalArgumentException("organizer must not be blank");
        }
        if (attendees <= 0) {
            throw new IllegalArgumentException("attendees must be positive");
        }
    }

    static Reservation confirm(RoomId roomId, String organizer, TimeSlot slot, int attendees) {
        return new Reservation(ReservationId.next(), roomId, organizer, slot, attendees,
                ReservationStatus.CONFIRMED);
    }

    public Reservation cancel(Instant now) {
        if (status == ReservationStatus.CANCELLED) {
            throw new ReservationAlreadyCancelledException(id.toString());
        }
        if (slot.hasStartedAt(now)) {
            throw new ReservationAlreadyStartedException(id.toString());
        }
        return new Reservation(id, roomId, organizer, slot, attendees, ReservationStatus.CANCELLED);
    }

    public boolean isConfirmed() {
        return status == ReservationStatus.CONFIRMED;
    }
}
