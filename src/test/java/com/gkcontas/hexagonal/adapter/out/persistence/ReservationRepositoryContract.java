package com.gkcontas.hexagonal.adapter.out.persistence;

import com.gkcontas.hexagonal.application.port.out.ReservationRepository;
import com.gkcontas.hexagonal.domain.model.Reservation;
import com.gkcontas.hexagonal.domain.model.ReservationId;
import com.gkcontas.hexagonal.domain.model.ReservationStatus;
import com.gkcontas.hexagonal.domain.model.RoomId;
import com.gkcontas.hexagonal.domain.model.TimeSlot;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * The contract of the outbound port, written once and run against every implementation.
 *
 * <p>"Two interchangeable adapters" is a claim, and this is what turns it into a fact. The
 * same assertions run against the map and against PostgreSQL; a behaviour that holds in
 * one and not the other shows up here instead of in production.
 *
 * <p>It matters most for the subtle parts. The half-open overlap window is implemented
 * twice — once as a stream filter, once as JPQL — and nothing but this class would notice
 * if one of them started treating a boundary as a conflict.
 */
abstract class ReservationRepositoryContract {

    protected static final RoomId AQUARIUM = new RoomId("AQUARIUM");
    protected static final RoomId ATRIUM = new RoomId("ATRIUM");

    protected abstract ReservationRepository repository();

    protected static Instant at(int hour) {
        return Instant.parse("2026-03-10T%02d:00:00Z".formatted(hour));
    }

    protected static Reservation reservation(RoomId roomId, int fromHour, int toHour,
                                             ReservationStatus status) {
        return new Reservation(ReservationId.next(), roomId, "ana",
                new TimeSlot(at(fromHour), at(toHour)), 4, status);
    }

    @Test
    void shouldStoreAndReadBackTheSameReservation() {
        Reservation saved = repository().save(reservation(AQUARIUM, 10, 11, ReservationStatus.CONFIRMED));

        assertThat(repository().findById(saved.id())).contains(saved);
    }

    @Test
    void shouldReturnEmptyForAnUnknownId() {
        assertThat(repository().findById(ReservationId.next())).isEmpty();
    }

    @Test
    void shouldReplaceTheReservationWhenSavingTheSameIdAgain() {
        Reservation saved = repository().save(reservation(AQUARIUM, 10, 11, ReservationStatus.CONFIRMED));

        repository().save(saved.cancel(at(9)));

        assertThat(repository().findById(saved.id()))
                .hasValueSatisfying(stored ->
                        assertThat(stored.status()).isEqualTo(ReservationStatus.CANCELLED));
    }

    @Test
    void shouldFindAReservationThatOverlapsTheWindow() {
        repository().save(reservation(AQUARIUM, 10, 12, ReservationStatus.CONFIRMED));

        assertThat(repository().findByRoomBetween(AQUARIUM, at(11), at(13))).hasSize(1);
    }

    @Test
    void shouldFindAReservationFullyInsideTheWindow() {
        repository().save(reservation(AQUARIUM, 10, 11, ReservationStatus.CONFIRMED));

        assertThat(repository().findByRoomBetween(AQUARIUM, at(9), at(13))).hasSize(1);
    }

    @Test
    void shouldNotReturnAReservationThatEndsExactlyWhenTheWindowStarts() {
        repository().save(reservation(AQUARIUM, 9, 10, ReservationStatus.CONFIRMED));

        // The boundary case. Both implementations have to agree that touching is not
        // overlapping, or back-to-back meetings break in one adapter and not the other.
        assertThat(repository().findByRoomBetween(AQUARIUM, at(10), at(11))).isEmpty();
    }

    @Test
    void shouldNotReturnAReservationThatStartsExactlyWhenTheWindowEnds() {
        repository().save(reservation(AQUARIUM, 11, 12, ReservationStatus.CONFIRMED));

        assertThat(repository().findByRoomBetween(AQUARIUM, at(10), at(11))).isEmpty();
    }

    @Test
    void shouldNotReturnReservationsFromAnotherRoom() {
        repository().save(reservation(ATRIUM, 10, 11, ReservationStatus.CONFIRMED));

        assertThat(repository().findByRoomBetween(AQUARIUM, at(9), at(13))).isEmpty();
    }

    @Test
    void shouldReturnCancelledReservationsToo() {
        repository().save(reservation(AQUARIUM, 10, 11, ReservationStatus.CANCELLED));

        // The port reports what is stored; deciding that a cancelled booking does not hold
        // the room is the domain's call, not the repository's.
        List<Reservation> found = repository().findByRoomBetween(AQUARIUM, at(9), at(13));
        assertThat(found).hasSize(1);
        assertThat(found.getFirst().isConfirmed()).isFalse();
    }

    @Test
    void shouldPreserveTheSlotExactly() {
        Reservation saved = repository().save(reservation(AQUARIUM, 10, 12, ReservationStatus.CONFIRMED));

        assertThat(repository().findById(saved.id()))
                .hasValueSatisfying(stored -> {
                    assertThat(stored.slot().start()).isEqualTo(at(10));
                    assertThat(stored.slot().duration()).isEqualTo(Duration.ofHours(2));
                });
    }
}
