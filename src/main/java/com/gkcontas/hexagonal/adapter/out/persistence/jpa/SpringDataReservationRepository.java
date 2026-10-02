package com.gkcontas.hexagonal.adapter.out.persistence.jpa;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/**
 * Spring Data lives here and stops here.
 *
 * <p>This interface is an implementation detail of the persistence adapter, not the port.
 * The port is {@code ReservationRepository}, in the application layer, and it knows
 * nothing about {@code JpaRepository} — which is what lets the in-memory adapter satisfy
 * it without pulling Spring Data along.
 */
interface SpringDataReservationRepository extends JpaRepository<ReservationJpaEntity, UUID> {

    /**
     * Half-open overlap, matching {@code TimeSlot.overlaps} exactly: a reservation ending
     * at the window's start does not count as inside it.
     */
    @Query("""
            SELECT r FROM ReservationJpaEntity r
            WHERE r.roomId = :roomId
              AND r.startsAt < :to
              AND r.endsAt > :from
            """)
    List<ReservationJpaEntity> findOverlapping(@Param("roomId") String roomId,
                                               @Param("from") Instant from,
                                               @Param("to") Instant to);
}
