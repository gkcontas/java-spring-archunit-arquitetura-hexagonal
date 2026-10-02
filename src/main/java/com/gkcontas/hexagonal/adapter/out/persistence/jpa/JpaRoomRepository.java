package com.gkcontas.hexagonal.adapter.out.persistence.jpa;

import com.gkcontas.hexagonal.application.port.out.RoomRepository;
import com.gkcontas.hexagonal.domain.model.Room;
import com.gkcontas.hexagonal.domain.model.RoomId;
import java.util.List;
import java.util.Optional;

public class JpaRoomRepository implements RoomRepository {

    private final SpringDataRoomRepository repository;

    JpaRoomRepository(SpringDataRoomRepository repository) {
        this.repository = repository;
    }

    @Override
    public Optional<Room> findById(RoomId id) {
        return repository.findById(id.value()).map(PersistenceMapper::toDomain);
    }

    @Override
    public List<Room> findAll() {
        return repository.findAll().stream().map(PersistenceMapper::toDomain).toList();
    }
}
