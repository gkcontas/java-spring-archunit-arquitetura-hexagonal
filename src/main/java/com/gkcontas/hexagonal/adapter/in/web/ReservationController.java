package com.gkcontas.hexagonal.adapter.in.web;

import com.gkcontas.hexagonal.application.port.in.BookRoomCommand;
import com.gkcontas.hexagonal.application.port.in.BookRoomUseCase;
import com.gkcontas.hexagonal.application.port.in.CancelReservationUseCase;
import com.gkcontas.hexagonal.application.port.in.FindReservationsUseCase;
import com.gkcontas.hexagonal.domain.model.ReservationId;
import com.gkcontas.hexagonal.domain.model.RoomId;
import jakarta.validation.Valid;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/**
 * One of two inbound adapters, and deliberately thin.
 *
 * <p>Its whole job is to translate: JSON into a command, a domain object into JSON, an
 * exception into a status code. It holds no rule — if a condition is worth checking here,
 * it is worth checking for the command line caller too, which means it belongs inside.
 */
@RestController
@RequestMapping("/reservations")
class ReservationController {

    private final BookRoomUseCase bookRoom;
    private final CancelReservationUseCase cancelReservation;
    private final FindReservationsUseCase findReservations;

    ReservationController(BookRoomUseCase bookRoom,
                          CancelReservationUseCase cancelReservation,
                          FindReservationsUseCase findReservations) {
        this.bookRoom = bookRoom;
        this.cancelReservation = cancelReservation;
        this.findReservations = findReservations;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    ReservationResponse book(@Valid @RequestBody BookReservationRequest request) {
        return ReservationResponse.from(bookRoom.book(new BookRoomCommand(
                request.roomId(),
                request.organizer(),
                request.start(),
                request.end(),
                request.attendees())));
    }

    @GetMapping("/{id}")
    ReservationResponse byId(@PathVariable String id) {
        return ReservationResponse.from(findReservations.byId(ReservationId.of(id)));
    }

    @DeleteMapping("/{id}")
    ReservationResponse cancel(@PathVariable String id) {
        return ReservationResponse.from(cancelReservation.cancel(ReservationId.of(id)));
    }

    @GetMapping
    List<ReservationResponse> byRoomAndDay(
            @RequestParam String roomId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam(defaultValue = "UTC") String zone) {
        return findReservations.byRoomAndDay(new RoomId(roomId), date, ZoneId.of(zone)).stream()
                .map(ReservationResponse::from)
                .toList();
    }
}
