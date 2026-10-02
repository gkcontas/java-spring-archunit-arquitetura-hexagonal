package com.gkcontas.hexagonal.application;

import com.gkcontas.hexagonal.adapter.out.persistence.memory.DirectTransactionRunner;
import com.gkcontas.hexagonal.adapter.out.persistence.memory.InMemoryReservationRepository;
import com.gkcontas.hexagonal.adapter.out.persistence.memory.InMemoryRoomRepository;
import com.gkcontas.hexagonal.application.port.in.BookRoomCommand;
import com.gkcontas.hexagonal.application.port.in.BookRoomUseCase;
import com.gkcontas.hexagonal.application.service.BookRoomService;
import com.gkcontas.hexagonal.domain.exception.RoomCapacityExceededException;
import com.gkcontas.hexagonal.domain.exception.RoomNotFoundException;
import com.gkcontas.hexagonal.domain.exception.RoomUnavailableException;
import com.gkcontas.hexagonal.domain.model.Reservation;
import com.gkcontas.hexagonal.domain.model.ReservationStatus;
import com.gkcontas.hexagonal.domain.model.Room;
import com.gkcontas.hexagonal.domain.model.RoomId;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * The use case tested with {@code new} and no mocking framework.
 *
 * <p>Two things make that possible, and both are consequences of the architecture: the
 * service is a plain class, so there is no context to start; and the outbound ports have
 * real in-memory implementations, so there is nothing to stub. A fake that genuinely
 * stores and queries catches mistakes a stub cannot — the stub returns what the test
 * author assumed, which is exactly the assumption under test.
 *
 * <p>The clock is fixed, so "a slot in the past" means the same thing on every run.
 */
class BookRoomServiceTest {

    private static final Instant NOW = Instant.parse("2026-03-10T09:00:00Z");

    private InMemoryReservationRepository reservations;
    private BookRoomUseCase bookRoom;

    @BeforeEach
    void setUp() {
        reservations = new InMemoryReservationRepository();
        InMemoryRoomRepository rooms = new InMemoryRoomRepository(List.of(
                new Room(new RoomId("AQUARIUM"), "Aquarium", 6),
                new Room(new RoomId("ATRIUM"), "Atrium", 20)));
        bookRoom = new BookRoomService(rooms, reservations, new DirectTransactionRunner(),
                Clock.fixed(NOW, ZoneOffset.UTC));
    }

    private static BookRoomCommand command(String room, int hour, int durationHours, int attendees) {
        Instant start = Instant.parse("2026-03-10T%02d:00:00Z".formatted(hour));
        return new BookRoomCommand(room, "ana", start, start.plus(Duration.ofHours(durationHours)),
                attendees);
    }

    @Test
    void shouldStoreTheReservationItReturns() {
        Reservation reservation = bookRoom.book(command("AQUARIUM", 10, 1, 4));

        assertThat(reservation.status()).isEqualTo(ReservationStatus.CONFIRMED);
        assertThat(reservations.findById(reservation.id())).contains(reservation);
    }

    @Test
    void shouldRefuseASecondBookingOverTheSameSlot() {
        bookRoom.book(command("AQUARIUM", 10, 2, 4));

        assertThatThrownBy(() -> bookRoom.book(command("AQUARIUM", 11, 1, 2)))
                .isInstanceOf(RoomUnavailableException.class);
    }

    @Test
    void shouldAllowTheSameSlotInAnotherRoom() {
        bookRoom.book(command("AQUARIUM", 10, 2, 4));

        assertThat(bookRoom.book(command("ATRIUM", 10, 2, 4))).isNotNull();
    }

    @Test
    void shouldRefuseAnUnknownRoom() {
        assertThatThrownBy(() -> bookRoom.book(command("BASEMENT", 10, 1, 2)))
                .isInstanceOf(RoomNotFoundException.class);
    }

    @Test
    void shouldRefuseMoreAttendeesThanTheRoomHolds() {
        assertThatThrownBy(() -> bookRoom.book(command("AQUARIUM", 10, 1, 7)))
                .isInstanceOf(RoomCapacityExceededException.class);
    }

    @Test
    void shouldNormaliseTheRoomIdentifier() {
        Reservation reservation = bookRoom.book(
                new BookRoomCommand("  aquarium ", "ana",
                        Instant.parse("2026-03-10T10:00:00Z"),
                        Instant.parse("2026-03-10T11:00:00Z"), 2));

        assertThat(reservation.roomId()).isEqualTo(new RoomId("AQUARIUM"));
    }

    @Test
    void shouldNotStoreAnythingWhenTheBookingIsRefused() {
        assertThatThrownBy(() -> bookRoom.book(command("AQUARIUM", 10, 1, 99)))
                .isInstanceOf(RoomCapacityExceededException.class);

        assertThat(reservations.findByRoomBetween(new RoomId("AQUARIUM"),
                Instant.parse("2026-03-10T00:00:00Z"),
                Instant.parse("2026-03-11T00:00:00Z"))).isEmpty();
    }
}
