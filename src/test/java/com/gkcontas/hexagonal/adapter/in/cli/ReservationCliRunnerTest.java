package com.gkcontas.hexagonal.adapter.in.cli;

import com.gkcontas.hexagonal.adapter.out.persistence.memory.DirectTransactionRunner;
import com.gkcontas.hexagonal.adapter.out.persistence.memory.InMemoryReservationRepository;
import com.gkcontas.hexagonal.adapter.out.persistence.memory.InMemoryRoomRepository;
import com.gkcontas.hexagonal.application.service.BookRoomService;
import com.gkcontas.hexagonal.application.service.CancelReservationService;
import com.gkcontas.hexagonal.application.service.FindReservationsService;
import com.gkcontas.hexagonal.application.service.ListRoomsService;
import com.gkcontas.hexagonal.domain.model.Room;
import com.gkcontas.hexagonal.domain.model.RoomId;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.DefaultApplicationArguments;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * The second inbound adapter, tested the same way as the first — except that this one
 * needs no Spring context at all.
 *
 * <p>The point of the class is in the setup: it builds the <em>same</em> use case objects
 * the REST controller receives, from the same in-memory adapters. Nothing in the
 * application layer had to change to gain a command line, which is the claim being
 * verified.
 */
class ReservationCliRunnerTest {

    private static final Instant NOW = Instant.parse("2026-03-10T09:00:00Z");

    private ByteArrayOutputStream output;
    private ReservationCliRunner runner;
    private InMemoryReservationRepository reservations;

    @BeforeEach
    void setUp() {
        output = new ByteArrayOutputStream();
        reservations = new InMemoryReservationRepository();
        InMemoryRoomRepository rooms = new InMemoryRoomRepository(List.of(
                new Room(new RoomId("AQUARIUM"), "Aquarium", 6),
                new Room(new RoomId("PHONE_BOOTH"), "Phone booth", 2)));
        Clock clock = Clock.fixed(NOW, ZoneOffset.UTC);
        DirectTransactionRunner transactions = new DirectTransactionRunner();

        runner = new ReservationCliRunner(
                new BookRoomService(rooms, reservations, transactions, clock),
                new CancelReservationService(reservations, transactions, clock),
                new FindReservationsService(reservations),
                new ListRoomsService(rooms),
                new PrintStream(output, true, StandardCharsets.UTF_8));
    }

    private String run(String... args) {
        output.reset();
        runner.run(new DefaultApplicationArguments(args));
        return output.toString(StandardCharsets.UTF_8);
    }

    @Test
    void shouldListTheRooms() {
        assertThat(run("rooms"))
                .contains("AQUARIUM")
                .contains("capacity 6")
                .contains("PHONE_BOOTH");
    }

    @Test
    void shouldBookAndStoreTheReservation() {
        String out = run("book", "--room=AQUARIUM", "--organizer=ana",
                "--from=2026-03-10T10:00:00Z", "--to=2026-03-10T11:00:00Z", "--attendees=4");

        assertThat(out).contains("AQUARIUM").contains("CONFIRMED");
        assertThat(reservations.findByRoomBetween(new RoomId("AQUARIUM"),
                Instant.parse("2026-03-10T00:00:00Z"),
                Instant.parse("2026-03-11T00:00:00Z"))).hasSize(1);
    }

    @Test
    void shouldReportTheSameBusinessFailureTheApiWouldReturn() {
        run("book", "--room=AQUARIUM", "--organizer=ana",
                "--from=2026-03-10T10:00:00Z", "--to=2026-03-10T12:00:00Z", "--attendees=4");

        String out = run("book", "--room=AQUARIUM", "--organizer=bruno",
                "--from=2026-03-10T11:00:00Z", "--to=2026-03-10T12:00:00Z", "--attendees=2");

        // Same exception the web adapter turns into 409, here printed as a line. The rule
        // was written once.
        assertThat(out).startsWith("error:").contains("already booked");
    }

    @Test
    void shouldReportACapacityFailure() {
        String out = run("book", "--room=PHONE_BOOTH", "--organizer=ana",
                "--from=2026-03-10T10:00:00Z", "--to=2026-03-10T11:00:00Z", "--attendees=5");

        assertThat(out).startsWith("error:").contains("the room holds 2");
    }

    @Test
    void shouldCancelByIdentifier() {
        String booked = run("book", "--room=AQUARIUM", "--organizer=ana",
                "--from=2026-03-10T10:00:00Z", "--to=2026-03-10T11:00:00Z", "--attendees=4");
        String id = booked.split("\\s+")[0];

        assertThat(run("cancel", "--id=" + id)).contains("CANCELLED");
    }

    @Test
    void shouldPrintTheAgendaOfADay() {
        run("book", "--room=AQUARIUM", "--organizer=ana",
                "--from=2026-03-10T10:00:00Z", "--to=2026-03-10T11:00:00Z", "--attendees=4");

        assertThat(run("agenda", "--room=AQUARIUM", "--date=2026-03-10")).contains("ana");
        assertThat(run("agenda", "--room=AQUARIUM", "--date=2026-03-11"))
                .contains("no reservations");
    }

    @Test
    void shouldComplainAboutAMissingOption() {
        assertThat(run("book", "--room=AQUARIUM"))
                .startsWith("error:")
                .contains("missing required option --organizer");
    }

    @Test
    void shouldPrintUsageWhenTheCommandIsUnknown() {
        assertThat(run("nonsense")).contains("usage:").contains("book");
    }
}
