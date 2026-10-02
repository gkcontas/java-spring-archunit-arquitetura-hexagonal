package com.gkcontas.hexagonal.domain;

import com.gkcontas.hexagonal.domain.exception.InvalidTimeSlotException;
import com.gkcontas.hexagonal.domain.exception.RoomCapacityExceededException;
import com.gkcontas.hexagonal.domain.exception.RoomUnavailableException;
import com.gkcontas.hexagonal.domain.model.Reservation;
import com.gkcontas.hexagonal.domain.model.ReservationStatus;
import com.gkcontas.hexagonal.domain.model.Room;
import com.gkcontas.hexagonal.domain.model.RoomId;
import com.gkcontas.hexagonal.domain.model.RoomSchedule;
import com.gkcontas.hexagonal.domain.model.TimeSlot;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class RoomScheduleTest {

    private static final Instant NOW = Instant.parse("2026-03-10T09:00:00Z");
    private static final Room AQUARIUM = new Room(new RoomId("AQUARIUM"), "Aquarium", 6);

    private static TimeSlot at(int hour, int durationHours) {
        Instant start = Instant.parse("2026-03-10T%02d:00:00Z".formatted(hour));
        return new TimeSlot(start, start.plus(Duration.ofHours(durationHours)));
    }

    @Test
    void shouldBookAFreeSlot() {
        RoomSchedule schedule = new RoomSchedule(AQUARIUM, List.of());

        Reservation reservation = schedule.book("ana", at(10, 1), 4, NOW);

        assertThat(reservation.roomId()).isEqualTo(AQUARIUM.id());
        assertThat(reservation.status()).isEqualTo(ReservationStatus.CONFIRMED);
        assertThat(reservation.id()).isNotNull();
    }

    @Test
    void shouldRefuseASlotThatOverlapsAConfirmedReservation() {
        RoomSchedule schedule = new RoomSchedule(AQUARIUM,
                List.of(reservation(at(10, 2), ReservationStatus.CONFIRMED)));

        assertThatThrownBy(() -> schedule.book("bruno", at(11, 1), 2, NOW))
                .isInstanceOf(RoomUnavailableException.class);
    }

    @Test
    void shouldAllowBookingOverACancelledReservation() {
        RoomSchedule schedule = new RoomSchedule(AQUARIUM,
                List.of(reservation(at(10, 2), ReservationStatus.CANCELLED)));

        // A cancelled booking does not hold the room. Forgetting to filter by status is
        // the quiet way a calendar fills up with slots nobody is using.
        assertThat(schedule.book("bruno", at(10, 2), 2, NOW)).isNotNull();
    }

    @Test
    void shouldAllowBackToBackBookings() {
        RoomSchedule schedule = new RoomSchedule(AQUARIUM,
                List.of(reservation(at(10, 1), ReservationStatus.CONFIRMED)));

        assertThat(schedule.book("bruno", at(11, 1), 2, NOW)).isNotNull();
    }

    @Test
    void shouldRefuseMoreAttendeesThanTheRoomHolds() {
        RoomSchedule schedule = new RoomSchedule(AQUARIUM, List.of());

        assertThatThrownBy(() -> schedule.book("ana", at(10, 1), 7, NOW))
                .isInstanceOf(RoomCapacityExceededException.class)
                .hasMessageContaining("the room holds 6");
    }

    @Test
    void shouldAcceptExactlyTheCapacity() {
        assertThat(new RoomSchedule(AQUARIUM, List.of()).book("ana", at(10, 1), 6, NOW))
                .isNotNull();
    }

    @Test
    void shouldRefuseASlotInThePast() {
        RoomSchedule schedule = new RoomSchedule(AQUARIUM, List.of());

        assertThatThrownBy(() -> schedule.book("ana", at(8, 1), 2, NOW))
                .isInstanceOf(InvalidTimeSlotException.class)
                .hasMessageContaining("past");
    }

    private static Reservation reservation(TimeSlot slot, ReservationStatus status) {
        return new Reservation(
                com.gkcontas.hexagonal.domain.model.ReservationId.next(),
                AQUARIUM.id(), "someone", slot, 2, status);
    }
}
