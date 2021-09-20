package io.vepo.kafka.load.parser;

import io.vepo.kafka.load.parser.exceptions.InvalidTestPlanException;
import io.vepo.kafka.load.parser.generated.TestPlanLexer;
import io.vepo.kafka.load.parser.generated.TestPlanParser;
import io.vepo.kafka.load.parser.internal.TestPlanCreator;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTreeWalker;

public class TestPlanFactory {

    private TestPlanFactory() {
    }

    public static TestPlan parseTestPlan(File contents) {
        try {
            return parseTestPlan(Files.readString(contents.toPath()));
        } catch (IOException ioe) {
         throw new InvalidTestPlanException("Cannot read file!", ioe);
        }
    }

    public static TestPlan parseTestPlan(String contents) {
        TestPlanParser parser = new TestPlanParser(
                new CommonTokenStream(new TestPlanLexer(CharStreams.fromString(contents))));
        ParseTreeWalker walker = new ParseTreeWalker();
        TestPlanCreator creator = new TestPlanCreator();
        walker.walk(creator, parser.plan());
        return creator.buildTestPlan();
    }
}
