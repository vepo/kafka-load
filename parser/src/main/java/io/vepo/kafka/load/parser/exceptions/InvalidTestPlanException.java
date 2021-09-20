package io.vepo.kafka.load.parser.exceptions;

import java.util.List;

public class InvalidTestPlanException extends RuntimeException {
    public InvalidTestPlanException(String reason) {
        super(reason);
    }

    public InvalidTestPlanException(String reason, Exception cause) {
        super(reason, cause);
    }

    public static <T> void requireNonNull(T value, String message) {
        if (value == null) {
            throw new InvalidTestPlanException(message);
        }
    }

    public static <T> void requiredNotEmpty(List<T> value, String message) {
        if (value == null || value.isEmpty()) {
            throw new InvalidTestPlanException(message);
        }
    }
}
