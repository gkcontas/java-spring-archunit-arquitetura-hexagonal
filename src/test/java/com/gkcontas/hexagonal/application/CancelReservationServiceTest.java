package com.gkcontas.hexagonal.application;

import com.gkcontas.hexagonal.adapter.out.persistence.memory.DirectTransactionRunner;
import com.gkcontas.hexagonal.adapter.out.persistence.memory.InMemoryReservationRepository;
import com.gkcontas.hexagonal.adapter.out.persistence.memory.InMemoryRoomRepository;
import com.gkcontas.hexagonal.application.port.in.BookRoomCommand;
import com.gkcontas.hexagonal.application.service.BookRoomService;
import com.gkcontas.hexagonal.application.service.CancelReservationService;
import com.gkcontas.hexagonal.domain.exception.ReservationAlreadyStartedException;
import com.gkcontas.hexagonal.domain.exception.ReservationNotFoundException;
import com.gkcontas.hexagonal.domain.model.Reservation;
import com.gkcontas.hexagonal.domain.model.ReservationId;
import com.gkcontas.hexagonal.domain.model.ReservationStatus;
import com.gkcontas.hexagonal.domain.model.Room;
import com.gkcontas.hexagonal.domain.model.RoomId;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CancelReservationServiceTest {

    private static final Instant NOW = Instant.parse("2026-03-10T09:00:00Z");

    private final InMemoryReservationRepository reservations = new InMemoryReservationRepository();
    private final InMemoryRoomRepository rooms = new InMemoryRoomRepository(
            List.of(new Room(new RoomId("ATRIUM"), "Atrium", 20)));

    private Reservation bookAtTen() {
        return new BookRoomService(rooms, reservations, new DirectTransactionRunner(),
                Clock.fixed(NOW, ZoneOffset.UTC))
                .book(new BookRoomCommand("ATRIUM", "ana",
                        Instant.parse("2026-03-10T10:00:00Z"),
                        Instant.parse("2026-03-10T11:00:00Z"), 4));
    }

    private CancelReservationService cancelAt(Instant now) {
        return new CancelReservationService(reservations, new DirectTransactionRunner(),
                Clock.fixed(now, ZoneOffset.UTC));
    }

    @Test
    void shouldCancelAndPersistTheNewState() {
        Reservation reservation = bookAtTen();

        Reservation cancelled = cancelAt(NOW).cancel(reservation.id());

        assertThat(cancelled.status()).isEqualTo(ReservationStatus.CANCELLED);
        assertThat(reservations.findById(reservation.id()))
                .hasValueSatisfying(stored ->
                        assertThat(stored.status()).isEqualTo(ReservationStatus.CANCELLED));
    }

    @Test
    void shouldFreeTheSlotForSomeoneElse() {
        Reservation reservation = bookAtTen();
        cancelAt(NOW).cancel(reservation.id());

        // The cancelled reservation is still stored; what matters is that the booking rule
        // stops counting it.
        assertThat(new BookRoomService(rooms, reservations, new DirectTransactionRunner(),
                Clock.fixed(NOW, ZoneOffset.UTC))
                .book(new BookRoomCommand("ATRIUM", "bruno",
                        Instant.parse("2026-03-10T10:00:00Z"),
                        Instant.parse("2026-03-10T11:00:00Z"), 2))).isNotNull();
    }

    @Test
    void shouldRefuseToCancelOnceTheMeetingHasStarted() {
        Reservation reservation = bookAtTen();

        assertThatThrownBy(() -> cancelAt(Instant.parse("2026-03-10T10:30:00Z"))
                .cancel(reservation.id()))
                .isInstanceOf(ReservationAlreadyStartedException.class);
    }

    @Test
    void shouldReportAnUnknownReservation() {
        assertThatThrownBy(() -> cancelAt(NOW).cancel(ReservationId.next()))
                .isInstanceOf(ReservationNotFoundException.class);
    }
}
