package io.vepo.kafka.load.parser;

public record Message(PropertyValue topic, PropertyValue key, PropertyValue value) {

    public static MessageBuilder builder() {
        return new MessageBuilder();
    }

    public static class MessageBuilder {
        private PropertyValue topic;
        private PropertyValue key;
        private PropertyValue value;

        private MessageBuilder() {

        }

        public MessageBuilder topic(PropertyValue topic) {
            this.topic = topic;
            return this;
        }

        public MessageBuilder key(PropertyValue key) {
            this.key = key;
            return this;
        }

        public MessageBuilder value(PropertyValue value) {
            this.value = value;
            return this;
        }

        public Message build() {
            return new Message(this.topic, this.key, this.value);
        }

    }


}
