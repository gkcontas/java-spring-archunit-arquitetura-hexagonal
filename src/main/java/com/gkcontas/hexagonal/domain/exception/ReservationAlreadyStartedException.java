package com.gkcontas.hexagonal.domain.exception;

public class ReservationAlreadyStartedException extends DomainException {

    public ReservationAlreadyStartedException(String reservationId) {
        super("Reservation %s has already started and can no longer be cancelled"
                .formatted(reservationId));
    }
}
