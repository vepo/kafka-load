package io.vepo.kafka.load.parser;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public record Assertion(PropertyValue topic, MessageAssertion[] assertions) {

    public static class AssertionBuilder {
        private PropertyValue topic;
        private List<MessageAssertion> assertions = new ArrayList<>();

        private AssertionBuilder() {
        }

        public AssertionBuilder topic(PropertyValue topic) {
            this.topic = topic;
            return this;
        }

        public AssertionBuilder assertion(MessageAssertion assertion) {
            this.assertions.add(assertion);
            return this;
        }

        public Assertion build() {
            return new Assertion(topic, assertions.toArray(MessageAssertion[]::new));
        }

    }

    public static AssertionBuilder builder() {
        return new AssertionBuilder();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Assertion assertion = (Assertion) o;
        return Objects.equals(topic, assertion.topic) &&
                Arrays.equals(assertions, assertion.assertions);
    }

    @Override
    public int hashCode() {
        int result = Objects.hash(topic);
        result = 31 * result + Arrays.hashCode(assertions);
        return result;
    }

    @Override
    public String toString() {
        return String.format("Assertion [topic=%s, assertions=%s]", topic, Arrays.deepToString(assertions));
    }
}
