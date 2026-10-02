package com.gkcontas.hexagonal.domain.model;

import java.util.Objects;
import java.util.UUID;

public record ReservationId(UUID value) {

    public ReservationId {
        Objects.requireNonNull(value, "reservation id must not be null");
    }

    public static ReservationId next() {
        return new ReservationId(UUID.randomUUID());
    }

    public static ReservationId of(String value) {
        return new ReservationId(UUID.fromString(value));
    }

    @Override
    public String toString() {
        return value.toString();
    }
}
