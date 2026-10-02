package com.gkcontas.hexagonal.adapter.in.web;

import com.gkcontas.hexagonal.domain.model.Reservation;
import java.time.Instant;

public record ReservationResponse(
        String id,
        String roomId,
        String organizer,
        Instant start,
        Instant end,
        int attendees,
        String status) {

    static ReservationResponse from(Reservation reservation) {
        return new ReservationResponse(
                reservation.id().toString(),
                reservation.roomId().value(),
                reservation.organizer(),
                reservation.slot().start(),
                reservation.slot().end(),
                reservation.attendees(),
                reservation.status().name());
    }
}
