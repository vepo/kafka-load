package io.vepo.kafka.load.parser;

public record Connection(PropertyValue bootstrapServer, MessageType produces, MessageType consumes) {
    public static class ConnectionBuilder {
        private PropertyValue bootstrapServer;
        private MessageType produces = MessageType.STRING;
        private MessageType consumes = MessageType.STRING;

        private ConnectionBuilder() {
        }

        public ConnectionBuilder bootstrapServer(PropertyValue bootstrapServer) {
            this.bootstrapServer = bootstrapServer;
            return this;
        }

        public ConnectionBuilder produces(MessageType produces) {
            this.produces = produces;
            return this;
        }

        public ConnectionBuilder consumes(MessageType consumes) {
            this.consumes = consumes;
            return this;
        }

        public Connection build() {
            return new Connection(this.bootstrapServer, this.produces, this.consumes);
        }
    }

    public static ConnectionBuilder builder() {
        return new ConnectionBuilder();
    }
}
