package com.gkcontas.hexagonal.domain.exception;

public class ReservationAlreadyCancelledException extends DomainException {

    public ReservationAlreadyCancelledException(String reservationId) {
        super("Reservation %s is already cancelled".formatted(reservationId));
    }
}
