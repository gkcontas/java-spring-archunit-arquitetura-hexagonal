package com.gkcontas.hexagonal.domain.exception;

public class InvalidTimeSlotException extends DomainException {

    public InvalidTimeSlotException(String message) {
        super(message);
    }
}
