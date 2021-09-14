package io.vepo.kafka.load.parser;

public record Connection(PropertyValue bootstrapServer) {
    public static class ConnectionBuilder {
        private PropertyValue bootstrapServer;

        private ConnectionBuilder() {
        }

        public ConnectionBuilder bootstrapServer(PropertyValue bootstrapServer) {
            this.bootstrapServer = bootstrapServer;
            return this;
        }

        public Connection build() {
            return new Connection(this.bootstrapServer);
        }
    }

    public static ConnectionBuilder builder() {
        return new ConnectionBuilder();
    }
}
