package com.gkcontas.hexagonal.application.port.out;

import com.gkcontas.hexagonal.domain.model.Room;
import com.gkcontas.hexagonal.domain.model.RoomId;
import java.util.List;
import java.util.Optional;

public interface RoomRepository {

    Optional<Room> findById(RoomId id);

    List<Room> findAll();
}
