package com.gkcontas.hexagonal.adapter.out.persistence.jpa;

import org.springframework.data.jpa.repository.JpaRepository;

interface SpringDataRoomRepository extends JpaRepository<RoomJpaEntity, String> {
}
