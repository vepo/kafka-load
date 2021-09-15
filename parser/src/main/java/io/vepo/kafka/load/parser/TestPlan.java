package io.vepo.kafka.load.parser;

import static io.vepo.kafka.load.parser.exceptions.InvalidTestPlanException.requiredNotEmpty;

import io.vepo.kafka.load.parser.exceptions.InvalidTestPlanException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public record TestPlan(String name, Connection connection, int clients, Duration cycleTime, Duration warmUp,
                       Duration execution, Duration rampDown, Step[] steps) {
    public static class TestPlanBuilder {

        private String name;
        private Connection connection;
        private int clients = 1;
        private Duration cycleTime = Duration.ofMillis(1);
        private Duration warmUp = Duration.ZERO;
        private Duration execution = Duration.ofSeconds(1);
        private Duration rampDown = Duration.ZERO;
        private List<Step> steps = new ArrayList<>();

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

        public TestPlanBuilder step(Step step) {
            this.steps.add(step);
            return this;
        }

        public TestPlan build() {
            requiredNotEmpty(this.steps, "No Step defined! You should define at least one step.");
            return new TestPlan(this.name, this.connection, this.clients, this.cycleTime, this.warmUp, this.execution,
                    this.rampDown, this.steps.toArray(Step[]::new));
        }
    }

    public static TestPlanBuilder builder() {
        return new TestPlanBuilder();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        TestPlan testPlan = (TestPlan) o;
        return clients == testPlan.clients && Objects.equals(name, testPlan.name) &&
                Objects.equals(connection, testPlan.connection) &&
                Objects.equals(cycleTime, testPlan.cycleTime) &&
                Objects.equals(warmUp, testPlan.warmUp) &&
                Objects.equals(execution, testPlan.execution) &&
                Objects.equals(rampDown, testPlan.rampDown) && Arrays.equals(steps, testPlan.steps);
    }

    @Override
    public int hashCode() {
        int result = Objects.hash(name, connection, clients, cycleTime, warmUp, execution, rampDown);
        result = 31 * result + Arrays.hashCode(steps);
        return result;
    }

    @Override
    public String toString() {
        return String
                .format("TestPlan [name=%s, connection=%s, clients=%d, cycleTime=%s, warmUp=%s, execution=%s, rampDown=%s, steps=%s]",
                        name, connection, clients, cycleTime, warmUp, execution, rampDown, Arrays.deepToString(steps));
    }
}
