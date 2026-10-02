package com.gkcontas.hexagonal.application.port.in;

import java.time.Instant;

/**
 * The input of a use case, in the vocabulary of the domain rather than of a protocol.
 *
 * <p>Deliberately not the HTTP request body. They look alike today and stop looking alike
 * the first time the API gains a field the use case does not need, or the CLI needs to
 * pass something the API does not expose. One extra record is what keeps a change in the
 * JSON contract from reaching the business rules.
 */
public record BookRoomCommand(
        String roomId,
        String organizer,
        Instant start,
        Instant end,
        int attendees) {
}
