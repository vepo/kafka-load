package io.vepo.kafka.load.engine;

import io.vepo.kafka.load.engine.config.Configuration;
import io.vepo.kafka.load.parser.TestPlan;

public class TestPanExecutor {

    public TestPanExecutor(Configuration configuration) {

    }


    public Result execute(TestPlan testPlan) {
        return Result.SUCCESS;
    }
}
