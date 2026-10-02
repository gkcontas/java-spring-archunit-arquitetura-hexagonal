package com.gkcontas.hexagonal.adapter.in.web;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.Instant;

/**
 * The HTTP contract, which is the web adapter's business and nobody else's.
 *
 * <p>Bean Validation here catches what is wrong with the <em>request</em> — a missing
 * field, a negative number — and answers 400 before any use case runs. What it cannot
 * catch is what is wrong with the <em>booking</em>: that the room is taken, or too small.
 * Those live in the domain, where the CLI gets them for free.
 */
public record BookReservationRequest(

        @NotBlank(message = "must not be blank")
        String roomId,

        @NotBlank(message = "must not be blank")
        String organizer,

        @NotNull(message = "must not be null")
        @Future(message = "must be in the future")
        Instant start,

        @NotNull(message = "must not be null")
        Instant end,

        @Min(value = 1, message = "must be at least 1")
        int attendees) {
}
