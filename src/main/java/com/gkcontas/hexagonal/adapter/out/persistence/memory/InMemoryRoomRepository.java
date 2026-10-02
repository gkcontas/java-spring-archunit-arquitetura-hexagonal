package com.gkcontas.hexagonal.adapter.out.persistence.memory;

import com.gkcontas.hexagonal.application.port.out.RoomRepository;
import com.gkcontas.hexagonal.domain.model.Room;
import com.gkcontas.hexagonal.domain.model.RoomId;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class InMemoryRoomRepository implements RoomRepository {

    private final Map<RoomId, Room> rooms = new LinkedHashMap<>();

    public InMemoryRoomRepository(List<Room> initialRooms) {
        initialRooms.forEach(room -> rooms.put(room.id(), room));
    }

    @Override
    public Optional<Room> findById(RoomId id) {
        return Optional.ofNullable(rooms.get(id));
    }

    @Override
    public List<Room> findAll() {
        return List.copyOf(rooms.values());
    }
}
