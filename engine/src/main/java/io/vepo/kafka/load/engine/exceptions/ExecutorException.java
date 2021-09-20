package io.vepo.kafka.load.engine.exceptions;

public class ExecutorException extends RuntimeException {

    public ExecutorException(String message, Exception cause) {
        super(message, cause);
    }
}
