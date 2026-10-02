package com.gkcontas.hexagonal.domain.exception;

/**
 * Base of every rule the domain refuses to break.
 *
 * <p>One hierarchy, so an adapter can translate business failures into its own protocol —
 * an HTTP status, an exit code — without the domain ever hearing about either. The domain
 * says what is wrong; deciding that "wrong" means 409 is a decision of the web adapter.
 */
public abstract class DomainException extends RuntimeException {

    protected DomainException(String message) {
        super(message);
    }
}
