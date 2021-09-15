package io.vepo.kafka.load.parser;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public record Step(String name, Message[] messages, Assertion[] assertions) {
    public static StepBuilder builder() {
        return new StepBuilder();
    }

    public static class StepBuilder {
        private String name;
        private List<Message> messages = new ArrayList<>();
        private List<Assertion> assertions = new ArrayList<>();

        private StepBuilder() {
        }

        public StepBuilder name(String name) {
            this.name = name;
            return this;
        }

        public StepBuilder message(Message message) {
            this.messages.add(message);
            return this;
        }

        public StepBuilder assertion(Assertion assertion) {
            this.assertions.add(assertion);
            return this;
        }

        public Step build() {
            return new Step(name, messages.toArray(Message[]::new), assertions.toArray(Assertion[]::new));
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Step step = (Step) o;
        return Objects.equals(name, step.name) && Arrays.equals(messages, step.messages) &&
                Arrays.equals(assertions, step.assertions);
    }

    @Override
    public int hashCode() {
        int result = Objects.hash(name);
        result = 31 * result + Arrays.hashCode(messages);
        result = 31 * result + Arrays.hashCode(assertions);
        return result;
    }

    @Override
    public String toString() {
        return String.format("Step [name=%s, messages=%s, assertions=%s]", name, Arrays.deepToString(messages), Arrays.deepToString(assertions));
    }
}
