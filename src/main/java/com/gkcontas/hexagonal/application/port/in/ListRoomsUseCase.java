package com.gkcontas.hexagonal.application.port.in;

import com.gkcontas.hexagonal.domain.model.Room;
import java.util.List;

public interface ListRoomsUseCase {

    List<Room> listAll();
}
