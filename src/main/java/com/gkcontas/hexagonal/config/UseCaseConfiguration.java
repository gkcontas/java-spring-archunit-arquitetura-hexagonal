package com.gkcontas.hexagonal.config;

import com.gkcontas.hexagonal.application.port.in.BookRoomUseCase;
import com.gkcontas.hexagonal.application.port.in.CancelReservationUseCase;
import com.gkcontas.hexagonal.application.port.in.FindReservationsUseCase;
import com.gkcontas.hexagonal.application.port.in.ListRoomsUseCase;
import com.gkcontas.hexagonal.application.port.out.ReservationRepository;
import com.gkcontas.hexagonal.application.port.out.RoomRepository;
import com.gkcontas.hexagonal.application.port.out.TransactionRunner;
import com.gkcontas.hexagonal.application.service.BookRoomService;
import com.gkcontas.hexagonal.application.service.CancelReservationService;
import com.gkcontas.hexagonal.application.service.FindReservationsService;
import com.gkcontas.hexagonal.application.service.ListRoomsService;
import java.time.Clock;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Where Spring is told how to build the application services — and the only reason they
 * need no annotations of their own.
 *
 * <p>The trade is explicit: a few lines of wiring here, in exchange for use cases that are
 * plain objects. A test constructs one with {@code new}, passing in-memory adapters, and
 * runs in milliseconds without a context. That is the practical payoff of the whole
 * arrangement, and it disappears the moment {@code @Service} and {@code @Autowired} move
 * into the application layer.
 */
@Configuration
public class UseCaseConfiguration {

    /**
     * Time as a dependency, never {@code Instant.now()} buried in a rule.
     *
     * <p>Half of this domain is about the future and the past. With an injected clock a
     * test can place "now" wherever it needs; without one, testing that a booking in the
     * past is refused means either sleeping or waiting for the machine to agree.
     */
    @Bean
    public Clock clock() {
        return Clock.systemUTC();
    }

    @Bean
    public BookRoomUseCase bookRoomUseCase(RoomRepository roomRepository,
                                           ReservationRepository reservationRepository,
                                           TransactionRunner transactionRunner,
                                           Clock clock) {
        return new BookRoomService(roomRepository, reservationRepository, transactionRunner, clock);
    }

    @Bean
    public CancelReservationUseCase cancelReservationUseCase(ReservationRepository reservationRepository,
                                                             TransactionRunner transactionRunner,
                                                             Clock clock) {
        return new CancelReservationService(reservationRepository, transactionRunner, clock);
    }

    @Bean
    public FindReservationsUseCase findReservationsUseCase(ReservationRepository reservationRepository) {
        return new FindReservationsService(reservationRepository);
    }

    @Bean
    public ListRoomsUseCase listRoomsUseCase(RoomRepository roomRepository) {
        return new ListRoomsService(roomRepository);
    }
}
