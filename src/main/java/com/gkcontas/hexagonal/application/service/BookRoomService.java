package com.gkcontas.hexagonal.application.service;

import com.gkcontas.hexagonal.application.port.in.BookRoomCommand;
import com.gkcontas.hexagonal.application.port.in.BookRoomUseCase;
import com.gkcontas.hexagonal.application.port.out.ReservationRepository;
import com.gkcontas.hexagonal.application.port.out.RoomRepository;
import com.gkcontas.hexagonal.application.port.out.TransactionRunner;
import com.gkcontas.hexagonal.domain.exception.RoomNotFoundException;
import com.gkcontas.hexagonal.domain.model.Reservation;
import com.gkcontas.hexagonal.domain.model.Room;
import com.gkcontas.hexagonal.domain.model.RoomId;
import com.gkcontas.hexagonal.domain.model.RoomSchedule;
import com.gkcontas.hexagonal.domain.model.TimeSlot;
import java.time.Clock;
import java.time.Instant;
import java.util.List;

/**
 * Orchestration, and nothing else.
 *
 * <p>Read what this class does and does not do: it loads what the decision needs, hands it
 * to the domain, stores the result. The rule about overlapping bookings is not here — it
 * is in {@link RoomSchedule}, where it can be tested without loading anything. A service
 * that starts accumulating {@code if}s about the business is the first sign the domain is
 * turning anaemic.
 *
 * <p>A plain class: no {@code @Service}, no {@code @Transactional}, no field injection.
 * It is turned into a bean by a configuration class in the outermost layer, which means
 * this one can be instantiated with {@code new} in a test — and that is exactly how the
 * tests do it.
 */
public class BookRoomService implements BookRoomUseCase {

    private final RoomRepository roomRepository;
    private final ReservationRepository reservationRepository;
    private final TransactionRunner transactionRunner;
    private final Clock clock;

    public BookRoomService(RoomRepository roomRepository,
                           ReservationRepository reservationRepository,
                           TransactionRunner transactionRunner,
                           Clock clock) {
        this.roomRepository = roomRepository;
        this.reservationRepository = reservationRepository;
        this.transactionRunner = transactionRunner;
        this.clock = clock;
    }

    @Override
    public Reservation book(BookRoomCommand command) {
        RoomId roomId = new RoomId(command.roomId());
        TimeSlot slot = new TimeSlot(command.start(), command.end());
        Instant now = clock.instant();

        return transactionRunner.inTransaction(() -> {
            Room room = roomRepository.findById(roomId)
                    .orElseThrow(() -> new RoomNotFoundException(roomId.value()));

            // Only the window that matters is loaded. Reading the room's whole history to
            // answer a question about one afternoon is how this kind of check turns into
            // the slowest endpoint of the system.
            List<Reservation> existing =
                    reservationRepository.findByRoomBetween(roomId, slot.start(), slot.end());

            Reservation reservation = new RoomSchedule(room, existing)
                    .book(command.organizer(), slot, command.attendees(), now);

            return reservationRepository.save(reservation);
        });
    }
}
