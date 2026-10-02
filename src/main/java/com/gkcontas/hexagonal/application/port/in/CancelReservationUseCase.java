package com.gkcontas.hexagonal.application.port.in;

import com.gkcontas.hexagonal.domain.model.Reservation;
import com.gkcontas.hexagonal.domain.model.ReservationId;

public interface CancelReservationUseCase {

    Reservation cancel(ReservationId id);
}
