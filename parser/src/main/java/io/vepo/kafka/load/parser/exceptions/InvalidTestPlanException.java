package io.vepo.kafka.load.exceptions;

public class InvalidTestPlanException extends RuntimeException {
    public InvalidTestPlanException(String reason) {
        super(reason);
    }
}
