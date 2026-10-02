package com.gkcontas.hexagonal.adapter.out.persistence.jpa;

import com.gkcontas.hexagonal.application.port.out.ReservationRepository;
import com.gkcontas.hexagonal.application.port.out.RoomRepository;
import com.gkcontas.hexagonal.application.port.out.TransactionRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

/**
 * Each adapter wires itself, under a profile.
 *
 * <p>Keeping this next to the classes it builds is what makes the adapter removable: the
 * whole persistence implementation, configuration included, is one directory. A single
 * central configuration class naming every implementation would quietly become the one
 * place that has to change whenever any adapter does.
 */
@Configuration
@Profile("jpa")
class JpaPersistenceConfiguration {

    @Bean
    ReservationRepository reservationRepository(SpringDataReservationRepository repository) {
        return new JpaReservationRepository(repository);
    }

    @Bean
    RoomRepository roomRepository(SpringDataRoomRepository repository) {
        return new JpaRoomRepository(repository);
    }

    @Bean
    TransactionRunner transactionRunner(PlatformTransactionManager transactionManager) {
        return new SpringTransactionRunner(new TransactionTemplate(transactionManager));
    }
}
