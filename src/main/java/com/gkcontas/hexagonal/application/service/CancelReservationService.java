package com.gkcontas.hexagonal.application.service;

import com.gkcontas.hexagonal.application.port.in.CancelReservationUseCase;
import com.gkcontas.hexagonal.application.port.out.ReservationRepository;
import com.gkcontas.hexagonal.application.port.out.TransactionRunner;
import com.gkcontas.hexagonal.domain.exception.ReservationNotFoundException;
import com.gkcontas.hexagonal.domain.model.Reservation;
import com.gkcontas.hexagonal.domain.model.ReservationId;
import java.time.Clock;

public class CancelReservationService implements CancelReservationUseCase {

    private final ReservationRepository reservationRepository;
    private final TransactionRunner transactionRunner;
    private final Clock clock;

    public CancelReservationService(ReservationRepository reservationRepository,
                                    TransactionRunner transactionRunner,
                                    Clock clock) {
        this.reservationRepository = reservationRepository;
        this.transactionRunner = transactionRunner;
        this.clock = clock;
    }

    @Override
    public Reservation cancel(ReservationId id) {
        return transactionRunner.inTransaction(() -> {
            Reservation reservation = reservationRepository.findById(id)
                    .orElseThrow(() -> new ReservationNotFoundException(id.toString()));
            // Whether it may be cancelled is the reservation's own business.
            return reservationRepository.save(reservation.cancel(clock.instant()));
        });
    }
}
