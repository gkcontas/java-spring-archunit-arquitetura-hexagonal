package com.gkcontas.hexagonal.adapter.out.persistence.jpa;

import com.gkcontas.hexagonal.domain.model.Reservation;
import com.gkcontas.hexagonal.domain.model.ReservationId;
import com.gkcontas.hexagonal.domain.model.ReservationStatus;
import com.gkcontas.hexagonal.domain.model.Room;
import com.gkcontas.hexagonal.domain.model.RoomId;
import com.gkcontas.hexagonal.domain.model.TimeSlot;

/**
 * The translation between the two models, written by hand and on purpose.
 *
 * <p>A mapping library would remove these lines and, with them, the place where a renamed
 * column announces itself as a compile error. At this size the explicit version is also
 * the one a reader can follow without learning another tool's conventions.
 */
final class PersistenceMapper {

    private PersistenceMapper() {
    }

    static Reservation toDomain(ReservationJpaEntity entity) {
        return new Reservation(
                new ReservationId(entity.getId()),
                new RoomId(entity.getRoomId()),
                entity.getOrganizer(),
                new TimeSlot(entity.getStartsAt(), entity.getEndsAt()),
                entity.getAttendees(),
                ReservationStatus.valueOf(entity.getStatus().name()));
    }

    static ReservationJpaEntity toEntity(Reservation reservation) {
        return new ReservationJpaEntity(
                reservation.id().value(),
                reservation.roomId().value(),
                reservation.organizer(),
                reservation.slot().start(),
                reservation.slot().end(),
                reservation.attendees(),
                ReservationJpaEntity.ReservationStatusJpa.valueOf(reservation.status().name()));
    }

    static Room toDomain(RoomJpaEntity entity) {
        return new Room(new RoomId(entity.getId()), entity.getName(), entity.getCapacity());
    }
}
