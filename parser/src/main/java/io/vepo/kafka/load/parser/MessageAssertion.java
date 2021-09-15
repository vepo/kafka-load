package io.vepo.kafka.load.parser;

public record MessageAssertion(String path, Operator operator, PropertyValue value) {

    public static class MessageAssertionBuilder {
        private String path;
        private Operator operator;
        private PropertyValue value;

        private MessageAssertionBuilder() {
        }

        public MessageAssertionBuilder path(String path) {
            this.path = path;
            return this;
        }

        public MessageAssertionBuilder operator(Operator operator) {
            this.operator = operator;
            return this;
        }

        public MessageAssertionBuilder value(PropertyValue value) {
            this.value = value;
            return this;
        }

        public MessageAssertion build() {
            return new MessageAssertion(path, operator, value);
        }
    }

    public static MessageAssertionBuilder builder() {
        return new MessageAssertionBuilder();
    }
}
