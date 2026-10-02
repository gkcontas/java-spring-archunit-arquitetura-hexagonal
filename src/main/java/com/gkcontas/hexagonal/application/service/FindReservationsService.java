package com.gkcontas.hexagonal.application.service;

import com.gkcontas.hexagonal.application.port.in.FindReservationsUseCase;
import com.gkcontas.hexagonal.application.port.out.ReservationRepository;
import com.gkcontas.hexagonal.domain.exception.ReservationNotFoundException;
import com.gkcontas.hexagonal.domain.model.Reservation;
import com.gkcontas.hexagonal.domain.model.ReservationId;
import com.gkcontas.hexagonal.domain.model.RoomId;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Comparator;
import java.util.List;

public class FindReservationsService implements FindReservationsUseCase {

    private final ReservationRepository reservationRepository;

    public FindReservationsService(ReservationRepository reservationRepository) {
        this.reservationRepository = reservationRepository;
    }

    @Override
    public Reservation byId(ReservationId id) {
        return reservationRepository.findById(id)
                .orElseThrow(() -> new ReservationNotFoundException(id.toString()));
    }

    @Override
    public List<Reservation> byRoomAndDay(RoomId roomId, LocalDate day, ZoneId zone) {
        // The day boundary depends on the caller's zone, so the zone is a parameter rather
        // than whatever the server happens to be configured with. The port below speaks
        // only in instants.
        Instant from = day.atStartOfDay(zone).toInstant();
        Instant to = day.plusDays(1).atStartOfDay(zone).toInstant();
        return reservationRepository.findByRoomBetween(roomId, from, to).stream()
                .sorted(Comparator.comparing(reservation -> reservation.slot().start()))
                .toList();
    }
}
