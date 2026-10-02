package com.gkcontas.hexagonal.adapter.out.persistence.jpa;

import com.gkcontas.hexagonal.application.port.out.TransactionRunner;
import java.util.function.Supplier;
import org.springframework.transaction.support.TransactionTemplate;

/**
 * The transaction port, backed by Spring.
 *
 * <p>{@code TransactionTemplate} rather than {@code @Transactional} because there is no
 * proxy involved: the application service is a plain object that calls this directly, so
 * the usual self-invocation trap of annotation-driven transactions does not exist here.
 */
public class SpringTransactionRunner implements TransactionRunner {

    private final TransactionTemplate transactionTemplate;

    public SpringTransactionRunner(TransactionTemplate transactionTemplate) {
        this.transactionTemplate = transactionTemplate;
    }

    @Override
    public <T> T inTransaction(Supplier<T> work) {
        return transactionTemplate.execute(status -> work.get());
    }
}
