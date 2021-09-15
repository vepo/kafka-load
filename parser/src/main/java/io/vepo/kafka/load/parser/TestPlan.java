package io.vepo.kafka.load.parser;

import io.vepo.kafka.load.parser.exceptions.InvalidTestPlanException;
import java.time.Duration;
import java.util.Objects;

public record TestPlan(String name, Connection connection, int clients, Duration cycleTime, Duration warmUp,
                       Duration execution, Duration rampDown) {
    public static class TestPlanBuilder {

        private String name;
        private Connection connection;
        private int clients = 1;
        private Duration cycleTime = Duration.ofMillis(1);
        private Duration warmUp = Duration.ZERO;
        private Duration execution = Duration.ofSeconds(1);
        private Duration rampDown = Duration.ZERO;

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

        public TestPlanBuilder clients(int clients) {
            if (clients <= 0) {
                throw new InvalidTestPlanException("Clients should be greater than 0");
            }
            this.clients = clients;
            return this;
        }

        public TestPlanBuilder cycleTime(Duration cycleTime) {
            Objects.requireNonNull(cycleTime, "Cycle Time should be greater than 0");
            this.cycleTime = cycleTime;
            return this;
        }

        public TestPlanBuilder warmUp(Duration warmUp) {
            Objects.requireNonNull(warmUp, "Warm Up should be greater than 0");
            this.warmUp = warmUp;
            return this;
        }


        public TestPlanBuilder execution(Duration execution) {
            Objects.requireNonNull(execution, "Execution should be greater than 0");
            this.execution = execution;
            return this;
        }

        public TestPlanBuilder rampDown(Duration rampDown) {
            Objects.requireNonNull(rampDown, "Ramp Down should be greater than 0");
            this.rampDown = rampDown;
            return this;
        }

        public TestPlan build() {
            return new TestPlan(this.name, this.connection, this.clients, this.cycleTime, this.warmUp, this.execution,
                    this.rampDown);
        }
    }

    public static TestPlanBuilder builder() {
        return new TestPlanBuilder();
    }
}
