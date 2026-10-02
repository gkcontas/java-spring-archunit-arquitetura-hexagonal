package com.gkcontas.hexagonal.adapter.out.persistence.jpa;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * The persistence model, which is not the domain model.
 *
 * <p>Two classes describing the same thing looks like duplication, and it is the price of
 * the boundary. The alternative — annotating {@code Reservation} with {@code @Entity} —
 * means the domain needs a no-arg constructor and mutable fields to satisfy Hibernate,
 * loses the ability to be a record, and acquires a dependency on Jakarta Persistence. The
 * invariants then have nowhere to live, because anything with setters can be put into an
 * invalid state after construction.
 *
 * <p>Here the mutable, annotated, framework-aware shape stays on this side of the wall.
 */
@Entity
@Table(name = "reservations")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ReservationJpaEntity {

    @Id
    private UUID id;

    @Column(name = "room_id", nullable = false, length = 40)
    private String roomId;

    @Column(nullable = false, length = 120)
    private String organizer;

    @Column(name = "starts_at", nullable = false)
    private Instant startsAt;

    @Column(name = "ends_at", nullable = false)
    private Instant endsAt;

    @Column(nullable = false)
    private int attendees;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ReservationStatusJpa status;

    public enum ReservationStatusJpa {
        CONFIRMED,
        CANCELLED
    }
}
