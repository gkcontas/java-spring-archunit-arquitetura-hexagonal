package com.gkcontas.hexagonal.application.port.out;

import java.util.function.Supplier;

/**
 * Transaction as an outbound port, so the core does not need {@code @Transactional}.
 *
 * <p>This is the compromise the hexagon usually makes quietly. Booking a room reads the
 * existing reservations and writes a new one, and those two steps have to be atomic —
 * which is an infrastructure concern expressed inside a use case. The common answer is to
 * annotate the application service with {@code @Transactional}, and it works; it also puts
 * a Spring annotation in the layer that is supposed to have no framework, and the
 * architecture test then has to make an exception for it.
 *
 * <p>Declaring it as a port keeps the rule intact: the application says "this runs as one
 * unit", the persistence adapter decides what that means. The JPA adapter delegates to
 * {@code TransactionTemplate}; the in-memory one just runs the block, which is honest —
 * there is nothing to commit.
 *
 * <p>The cost is one more indirection, and it is worth naming rather than hiding: a team
 * that would rather accept the annotation is making a defensible choice, not a mistake.
 */
public interface TransactionRunner {

    <T> T inTransaction(Supplier<T> work);
}
