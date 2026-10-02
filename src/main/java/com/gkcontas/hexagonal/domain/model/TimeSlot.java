package com.gkcontas.hexagonal.domain.model;

import com.gkcontas.hexagonal.domain.exception.InvalidTimeSlotException;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Objects;

/**
 * An interval that cannot be built in an invalid state.
 *
 * <p>Validating in the constructor rather than in a service is what makes the rest of the
 * code simpler: every method that receives a {@code TimeSlot} already knows the end comes
 * after the start and the duration is within bounds. The alternative — two loose
 * {@code Instant}s plus a validator somebody has to remember to call — moves the check to
 * every call site and eventually misses one.
 */
public record TimeSlot(Instant start, Instant end) {

    public static final Duration MINIMUM = Duration.ofMinutes(15);
    public static final Duration MAXIMUM = Duration.ofHours(8);

    private static final DateTimeFormatter FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm").withZone(ZoneOffset.UTC);

    public TimeSlot {
        Objects.requireNonNull(start, "start must not be null");
        Objects.requireNonNull(end, "end must not be null");
        if (!end.isAfter(start)) {
            throw new InvalidTimeSlotException("The end must come after the start");
        }
        Duration duration = Duration.between(start, end);
        if (duration.compareTo(MINIMUM) < 0) {
            throw new InvalidTimeSlotException(
                    "A booking must last at least %d minutes".formatted(MINIMUM.toMinutes()));
        }
        if (duration.compareTo(MAXIMUM) > 0) {
            throw new InvalidTimeSlotException(
                    "A booking must last at most %d hours".formatted(MAXIMUM.toHours()));
        }
    }

    public Duration duration() {
        return Duration.between(start, end);
    }

    /**
     * Half-open interval: a slot ending at 10:00 and one starting at 10:00 do not overlap.
     *
     * <p>Treating the boundary as a conflict would make back-to-back meetings impossible,
     * which is the first thing a user tries. Getting this wrong by one operator is the
     * classic off-by-one of scheduling code, and it is why the test for it is explicit.
     */
    public boolean overlaps(TimeSlot other) {
        return start.isBefore(other.end) && other.start.isBefore(end);
    }

    public boolean startsBefore(Instant moment) {
        return start.isBefore(moment);
    }

    public boolean hasStartedAt(Instant moment) {
        return !start.isAfter(moment);
    }

    @Override
    public String toString() {
        return "%s - %s".formatted(FORMATTER.format(start), FORMATTER.format(end));
    }
}
