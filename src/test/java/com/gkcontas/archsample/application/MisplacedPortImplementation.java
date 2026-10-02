package com.gkcontas.archsample.application;

import com.gkcontas.hexagonal.application.port.out.RoomRepository;
import com.gkcontas.hexagonal.domain.model.Room;
import com.gkcontas.hexagonal.domain.model.RoomId;
import java.util.List;
import java.util.Optional;

/**
 * An outbound port implemented inside the application layer instead of in an adapter —
 * the shortcut that quietly removes the seam the port exists to create.
 */
public class MisplacedPortImplementation implements RoomRepository {

    @Override
    public Optional<Room> findById(RoomId id) {
        return Optional.empty();
    }

    @Override
    public List<Room> findAll() {
        return List.of();
    }
}
