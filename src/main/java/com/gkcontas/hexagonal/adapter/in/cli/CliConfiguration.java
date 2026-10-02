package com.gkcontas.hexagonal.adapter.in.cli;

import com.gkcontas.hexagonal.application.port.in.BookRoomUseCase;
import com.gkcontas.hexagonal.application.port.in.CancelReservationUseCase;
import com.gkcontas.hexagonal.application.port.in.FindReservationsUseCase;
import com.gkcontas.hexagonal.application.port.in.ListRoomsUseCase;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

/**
 * The command line adapter, active only under the {@code cli} profile.
 *
 * <p>Same pattern as the persistence adapters: the adapter owns its wiring, so the whole
 * thing is one directory and switching it off is one profile.
 */
@Configuration
@Profile("cli")
class CliConfiguration {

    @Bean
    ReservationCliRunner reservationCliRunner(BookRoomUseCase bookRoom,
                                              CancelReservationUseCase cancelReservation,
                                              FindReservationsUseCase findReservations,
                                              ListRoomsUseCase listRooms) {
        return new ReservationCliRunner(bookRoom, cancelReservation, findReservations,
                listRooms, System.out);
    }
}
