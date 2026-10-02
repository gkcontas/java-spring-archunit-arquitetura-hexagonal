package com.gkcontas.hexagonal.adapter.out.persistence.memory;

import com.gkcontas.hexagonal.application.port.out.TransactionRunner;
import java.util.function.Supplier;

/**
 * Runs the block and nothing more.
 *
 * <p>Honest rather than clever: there is no transaction to open over a map, so this does
 * not pretend there is. Which is also the point — the application asked for atomicity and
 * the adapter answered with what its storage can actually offer. Anything that needs real
 * isolation is tested against the JPA adapter.
 */
public class DirectTransactionRunner implements TransactionRunner {

    @Override
    public <T> T inTransaction(Supplier<T> work) {
        return work.get();
    }
}
