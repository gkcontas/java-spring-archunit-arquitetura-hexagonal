package com.gkcontas.hexagonal.domain.exception;

/**
 * Takes plain values rather than {@code RoomId} and {@code TimeSlot}, and that is not an
 * oversight.
 *
 * <p>Typed parameters read better at the throw site and create a dependency from
 * {@code domain.exception} back to {@code domain.model} — which, together with the model
 * throwing these, is a cycle between the two packages. The cycle check caught it. Breaking
 * it costs one {@code toString()} at the call site and leaves the arrow pointing one way:
 * the model knows its failures, the failures know nothing.
 */
public class RoomUnavailableException extends DomainException {

    public RoomUnavailableException(String roomId, String slot) {
        super("Room %s is already booked during %s".formatted(roomId, slot));
    }
}
