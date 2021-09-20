package io.vepo.kafka.load.runtime;

import io.vepo.kafka.load.engine.Result;
import java.io.File;
import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Stream;

import io.vepo.kafka.load.engine.TestPanExecutor;
import io.vepo.kafka.load.engine.config.Configuration;
import io.vepo.kafka.load.parser.TestPlanFactory;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;


@Command(name = "kafka-load", mixinStandardHelpOptions = true, version = "Kafka Load 1.0.0",
        description = "Load testing tool for Kafka applications.")
public class KafkaLoad implements Callable<Integer> {

    @Parameters(paramLabel = "TEST PLAN", description = "One or more test plan to be executed in order")
    private File[] testPlans;

    @Option(names = {"-p", "--properties"}, description = "Properties file used on Test Plan")
    private File propertiesFile;

    public static void main(String[] args) {
        int exitCode = new CommandLine(new KafkaLoad()).execute(args);
        System.exit(exitCode);
    }

    @Override
    public Integer call() throws Exception {
        var executor =
                new TestPanExecutor(Optional.ofNullable(propertiesFile)
                        .map(Configuration::fromFile)
                        .orElseGet(Configuration::empty));
        AtomicBoolean success = new AtomicBoolean(true);
        Stream.of(testPlans).map(TestPlanFactory::parseTestPlan)
                .forEachOrdered(testPlan -> {
                    if (success.get()) {
                        var result = executor.execute(testPlan);
                        if (result != Result.SUCCESS) {
                            success.set(false);
                        }
                    }
                });
        return success.get() ? 0 : 1;
    }
}