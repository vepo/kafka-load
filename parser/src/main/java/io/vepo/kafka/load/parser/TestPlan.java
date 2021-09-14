package io.vepo.kafka.load.parser;

public record TestPlan(String name, Connection connection) {
    public static class TestPlanBuilder {

        private String name;
        private Connection connection;

        private TestPlanBuilder() {
        }

        public TestPlanBuilder name(String name) {
            this.name = name;
            return this;
        }

        public TestPlanBuilder connection(Connection connection) {
            this.connection = connection;
            return this;
        }

        public TestPlan build() {
            return new TestPlan(this.name, this.connection);
        }
    }

    public static TestPlanBuilder builder() {
        return new TestPlanBuilder();
    }
}
