package com.gkcontas.hexagonal.adapter.in.web;

import com.gkcontas.hexagonal.application.port.in.ListRoomsUseCase;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/rooms")
class RoomController {

    private final ListRoomsUseCase listRooms;

    RoomController(ListRoomsUseCase listRooms) {
        this.listRooms = listRooms;
    }

    @GetMapping
    List<RoomResponse> list() {
        return listRooms.listAll().stream().map(RoomResponse::from).toList();
    }
}
