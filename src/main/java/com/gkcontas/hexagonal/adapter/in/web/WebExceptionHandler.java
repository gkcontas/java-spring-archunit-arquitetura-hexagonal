package com.gkcontas.hexagonal.adapter.in.web;

import com.gkcontas.hexagonal.domain.exception.InvalidTimeSlotException;
import com.gkcontas.hexagonal.domain.exception.ReservationAlreadyCancelledException;
import com.gkcontas.hexagonal.domain.exception.ReservationAlreadyStartedException;
import com.gkcontas.hexagonal.domain.exception.ReservationNotFoundException;
import com.gkcontas.hexagonal.domain.exception.RoomCapacityExceededException;
import com.gkcontas.hexagonal.domain.exception.RoomNotFoundException;
import com.gkcontas.hexagonal.domain.exception.RoomUnavailableException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.validation.BindException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * Where a business failure becomes an HTTP status.
 *
 * <p>This translation is the web adapter's responsibility precisely because it is not
 * universal: "the room is taken" is a 409 over HTTP and an exit code over the command
 * line. The domain throws {@code RoomUnavailableException} and stays out of the argument.
 */
@RestControllerAdvice
class WebExceptionHandler {

    @ExceptionHandler({ReservationNotFoundException.class, RoomNotFoundException.class})
    ProblemDetail handleNotFound(RuntimeException exception) {
        return problem(HttpStatus.NOT_FOUND, "Not found", exception.getMessage());
    }

    @ExceptionHandler(RoomUnavailableException.class)
    ProblemDetail handleUnavailable(RoomUnavailableException exception) {
        // 409: the request is well-formed, the world just disagrees with it.
        return problem(HttpStatus.CONFLICT, "Room unavailable", exception.getMessage());
    }

    @ExceptionHandler({ReservationAlreadyStartedException.class,
            ReservationAlreadyCancelledException.class})
    ProblemDetail handleConflict(RuntimeException exception) {
        return problem(HttpStatus.CONFLICT, "Reservation cannot be cancelled", exception.getMessage());
    }

    @ExceptionHandler({InvalidTimeSlotException.class, RoomCapacityExceededException.class,
            IllegalArgumentException.class})
    ProblemDetail handleBadRequest(RuntimeException exception) {
        return problem(HttpStatus.BAD_REQUEST, "Invalid booking", exception.getMessage());
    }

    @ExceptionHandler(BindException.class)
    ProblemDetail handleValidation(BindException exception) {
        String detail = exception.getFieldErrors().stream()
                .map(error -> "%s %s".formatted(error.getField(), error.getDefaultMessage()))
                .sorted()
                .reduce((left, right) -> left + "; " + right)
                .orElse("The request is not valid.");
        return problem(HttpStatus.BAD_REQUEST, "Invalid request", detail);
    }

    private static ProblemDetail problem(HttpStatus status, String title, String detail) {
        ProblemDetail problemDetail = ProblemDetail.forStatus(status);
        problemDetail.setTitle(title);
        problemDetail.setDetail(detail);
        return problemDetail;
    }
}
