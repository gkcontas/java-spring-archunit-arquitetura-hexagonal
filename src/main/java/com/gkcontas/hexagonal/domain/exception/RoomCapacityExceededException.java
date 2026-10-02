package com.gkcontas.hexagonal.domain.exception;

public class RoomCapacityExceededException extends DomainException {

    public RoomCapacityExceededException(int attendees, int capacity) {
        super("Cannot book for %d attendees: the room holds %d".formatted(attendees, capacity));
    }
}
