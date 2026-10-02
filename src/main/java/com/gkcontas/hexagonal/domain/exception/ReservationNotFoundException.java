package com.gkcontas.hexagonal.domain.exception;

public class ReservationNotFoundException extends DomainException {

    public ReservationNotFoundException(String reservationId) {
        super("No reservation with id %s".formatted(reservationId));
    }
}
