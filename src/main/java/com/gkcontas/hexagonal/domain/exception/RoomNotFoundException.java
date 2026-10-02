package com.gkcontas.hexagonal.domain.exception;

public class RoomNotFoundException extends DomainException {

    public RoomNotFoundException(String roomId) {
        super("No room with id %s".formatted(roomId));
    }
}
