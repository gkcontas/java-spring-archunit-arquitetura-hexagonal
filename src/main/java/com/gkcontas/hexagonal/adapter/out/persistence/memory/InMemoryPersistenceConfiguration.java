package com.gkcontas.hexagonal.adapter.out.persistence.memory;

import com.gkcontas.hexagonal.application.port.out.ReservationRepository;
import com.gkcontas.hexagonal.application.port.out.RoomRepository;
import com.gkcontas.hexagonal.application.port.out.TransactionRunner;
import com.gkcontas.hexagonal.domain.model.Room;
import com.gkcontas.hexagonal.domain.model.RoomId;
import java.util.List;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

/**
 * The same rooms the Flyway migration seeds, so switching profiles changes the storage and
 * not the fixtures.
 */
@Configuration
@Profile("memory")
class InMemoryPersistenceConfiguration {

    @Bean
    ReservationRepository reservationRepository() {
        return new InMemoryReservationRepository();
    }

    @Bean
    RoomRepository roomRepository() {
        return new InMemoryRoomRepository(List.of(
                new Room(new RoomId("AQUARIUM"), "Aquarium", 6),
                new Room(new RoomId("ATRIUM"), "Atrium", 20),
                new Room(new RoomId("BOARDROOM"), "Boardroom", 12),
                new Room(new RoomId("PHONE_BOOTH"), "Phone booth", 2)));
    }

    @Bean
    TransactionRunner transactionRunner() {
        return new DirectTransactionRunner();
    }
}
