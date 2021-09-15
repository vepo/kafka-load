package io.vepo.kafka.load.parser.exceptions;

public class InvalidTestPlanException extends RuntimeException {
    public InvalidTestPlanException(String reason) {
        super(reason);
    }
}
