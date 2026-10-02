package com.gkcontas.hexagonal.domain.model;

import java.util.Objects;

/**
 * A typed identifier instead of a bare {@code String}.
 *
 * <p>It costs one record and removes an entire class of bug: a method that takes
 * {@code (RoomId, ReservationId)} cannot be called with the arguments swapped, while one
 * that takes {@code (String, String)} can — and the compiler has nothing to say about it.
 */
public record RoomId(String value) {

    public RoomId {
        Objects.requireNonNull(value, "room id must not be null");
        value = value.trim().toUpperCase();
        if (value.isEmpty()) {
            throw new IllegalArgumentException("room id must not be blank");
        }
    }

    @Override
    public String toString() {
        return value;
    }
}
