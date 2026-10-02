package com.gkcontas.hexagonal.domain.model;

import java.util.Objects;

public record Room(RoomId id, String name, int capacity) {

    public Room {
        Objects.requireNonNull(id, "room id must not be null");
        Objects.requireNonNull(name, "room name must not be null");
        if (capacity <= 0) {
            throw new IllegalArgumentException("capacity must be positive");
        }
    }

    public boolean holds(int attendees) {
        return attendees <= capacity;
    }
}
