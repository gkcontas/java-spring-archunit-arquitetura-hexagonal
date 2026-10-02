package com.gkcontas.hexagonal.domain;

import com.gkcontas.hexagonal.domain.exception.ReservationAlreadyCancelledException;
import com.gkcontas.hexagonal.domain.exception.ReservationAlreadyStartedException;
import com.gkcontas.hexagonal.domain.model.Reservation;
import com.gkcontas.hexagonal.domain.model.ReservationId;
import com.gkcontas.hexagonal.domain.model.ReservationStatus;
import com.gkcontas.hexagonal.domain.model.RoomId;
import com.gkcontas.hexagonal.domain.model.TimeSlot;
import java.time.Duration;
import java.time.Instant;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ReservationTest {

    private static final Instant START = Instant.parse("2026-03-10T14:00:00Z");

    private static Reservation confirmed() {
        return new Reservation(ReservationId.next(), new RoomId("ATRIUM"), "ana",
                new TimeSlot(START, START.plus(Duration.ofHours(1))), 5,
                ReservationStatus.CONFIRMED);
    }

    @Test
    void shouldCancelBeforeItStarts() {
        Reservation cancelled = confirmed().cancel(START.minus(Duration.ofMinutes(1)));

        assertThat(cancelled.status()).isEqualTo(ReservationStatus.CANCELLED);
        assertThat(cancelled.isConfirmed()).isFalse();
    }

    @Test
    void shouldReturnANewInstanceRatherThanMutate() {
        Reservation original = confirmed();

        Reservation cancelled = original.cancel(START.minus(Duration.ofHours(1)));

        // Immutability is why nothing else can be holding a reservation that silently
        // changed state under it.
        assertThat(original.status()).isEqualTo(ReservationStatus.CONFIRMED);
        assertThat(cancelled).isNotSameAs(original);
        assertThat(cancelled.id()).isEqualTo(original.id());
    }

    @Test
    void shouldRefuseToCancelOnceItHasStarted() {
        assertThatThrownBy(() -> confirmed().cancel(START))
                .isInstanceOf(ReservationAlreadyStartedException.class);
    }

    @Test
    void shouldRefuseToCancelTwice() {
        Reservation cancelled = confirmed().cancel(START.minus(Duration.ofHours(1)));

        assertThatThrownBy(() -> cancelled.cancel(START.minus(Duration.ofHours(1))))
                .isInstanceOf(ReservationAlreadyCancelledException.class);
    }
}
