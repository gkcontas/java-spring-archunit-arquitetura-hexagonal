package com.gkcontas.hexagonal.application.service;

import com.gkcontas.hexagonal.application.port.in.ListRoomsUseCase;
import com.gkcontas.hexagonal.application.port.out.RoomRepository;
import com.gkcontas.hexagonal.domain.model.Room;
import java.util.Comparator;
import java.util.List;

public class ListRoomsService implements ListRoomsUseCase {

    private final RoomRepository roomRepository;

    public ListRoomsService(RoomRepository roomRepository) {
        this.roomRepository = roomRepository;
    }

    @Override
    public List<Room> listAll() {
        return roomRepository.findAll().stream()
                .sorted(Comparator.comparing(room -> room.id().value()))
                .toList();
    }
}
