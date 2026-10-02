package com.gkcontas.hexagonal.application.port.in;

import com.gkcontas.hexagonal.domain.model.Reservation;
import com.gkcontas.hexagonal.domain.model.ReservationId;
import com.gkcontas.hexagonal.domain.model.RoomId;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;

public interface FindReservationsUseCase {

    Reservation byId(ReservationId id);

    List<Reservation> byRoomAndDay(RoomId roomId, LocalDate day, ZoneId zone);
}
