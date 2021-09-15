package io.vepo.kafka.load.parser;

public record Step(String name) {
    public static StepBuilder builder() {
        return new StepBuilder();
    }

    public static class StepBuilder {
        private String name;

        private StepBuilder() {
        }

        public StepBuilder name(String name) {
            this.name = name;
            return this;
        }

        public Step build() {
            return new Step(this.name);
        }

    }
}
