package com.gkcontas.hexagonal.domain;

import com.gkcontas.hexagonal.domain.exception.InvalidTimeSlotException;
import com.gkcontas.hexagonal.domain.model.TimeSlot;
import java.time.Duration;
import java.time.Instant;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Pure unit tests: no Spring, no database, no mock.
 *
 * <p>That this is possible is the measurable benefit of keeping the rules in the domain.
 * The whole class runs in milliseconds because there is nothing to start.
 */
class TimeSlotTest {

    private static final Instant TEN = Instant.parse("2026-03-10T10:00:00Z");

    @Test
    void shouldRefuseAnEndBeforeTheStart() {
        assertThatThrownBy(() -> new TimeSlot(TEN, TEN.minus(Duration.ofHours(1))))
                .isInstanceOf(InvalidTimeSlotException.class)
                .hasMessageContaining("end must come after the start");
    }

    @Test
    void shouldRefuseAnEmptySlot() {
        assertThatThrownBy(() -> new TimeSlot(TEN, TEN))
                .isInstanceOf(InvalidTimeSlotException.class);
    }

    @Test
    void shouldRefuseSomethingShorterThanTheMinimum() {
        assertThatThrownBy(() -> new TimeSlot(TEN, TEN.plus(Duration.ofMinutes(10))))
                .isInstanceOf(InvalidTimeSlotException.class)
                .hasMessageContaining("at least 15 minutes");
    }

    @Test
    void shouldRefuseSomethingLongerThanTheMaximum() {
        assertThatThrownBy(() -> new TimeSlot(TEN, TEN.plus(Duration.ofHours(9))))
                .isInstanceOf(InvalidTimeSlotException.class)
                .hasMessageContaining("at most 8 hours");
    }

    @Test
    void shouldAcceptExactlyTheBoundaries() {
        assertThat(new TimeSlot(TEN, TEN.plus(Duration.ofMinutes(15))).duration())
                .isEqualTo(Duration.ofMinutes(15));
        assertThat(new TimeSlot(TEN, TEN.plus(Duration.ofHours(8))).duration())
                .isEqualTo(Duration.ofHours(8));
    }

    @Test
    void shouldDetectAnOverlap() {
        TimeSlot morning = new TimeSlot(TEN, TEN.plus(Duration.ofHours(2)));
        TimeSlot middle = new TimeSlot(TEN.plus(Duration.ofHours(1)), TEN.plus(Duration.ofHours(3)));

        assertThat(morning.overlaps(middle)).isTrue();
        assertThat(middle.overlaps(morning)).as("overlap is symmetric").isTrue();
    }

    @Test
    void shouldTreatBackToBackSlotsAsFree() {
        TimeSlot first = new TimeSlot(TEN, TEN.plus(Duration.ofHours(1)));
        TimeSlot second = new TimeSlot(TEN.plus(Duration.ofHours(1)), TEN.plus(Duration.ofHours(2)));

        // The interval is half-open. If the boundary counted as a conflict, two meetings
        // could never be scheduled one right after the other — the first thing anybody
        // tries to do with a booking system.
        assertThat(first.overlaps(second)).isFalse();
        assertThat(second.overlaps(first)).isFalse();
    }

    @Test
    void shouldDetectAContainedSlot() {
        TimeSlot wide = new TimeSlot(TEN, TEN.plus(Duration.ofHours(4)));
        TimeSlot inside = new TimeSlot(TEN.plus(Duration.ofHours(1)), TEN.plus(Duration.ofHours(2)));

        assertThat(wide.overlaps(inside)).isTrue();
        assertThat(inside.overlaps(wide)).isTrue();
    }
}
