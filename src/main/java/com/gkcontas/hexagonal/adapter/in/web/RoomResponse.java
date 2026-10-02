package com.gkcontas.hexagonal.adapter.in.web;

import com.gkcontas.hexagonal.domain.model.Room;

public record RoomResponse(String id, String name, int capacity) {

    static RoomResponse from(Room room) {
        return new RoomResponse(room.id().value(), room.name(), room.capacity());
    }
}
