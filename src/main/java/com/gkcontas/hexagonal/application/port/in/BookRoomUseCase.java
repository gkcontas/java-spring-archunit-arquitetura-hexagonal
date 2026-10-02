package com.gkcontas.hexagonal.application.port.in;

import com.gkcontas.hexagonal.domain.model.Reservation;

/**
 * An inbound port: what the application offers, stated as an interface.
 *
 * <p>The REST controller and the command line runner both depend on this and on nothing
 * else of the inside. That is what makes "two input adapters" more than a slogan — adding
 * a third, a message consumer say, is a new class in {@code adapter.in} and zero changes
 * anywhere else.
 */
public interface BookRoomUseCase {

    Reservation book(BookRoomCommand command);
}
