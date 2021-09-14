package io.vepo.kafka.load.parser.internal;

import static java.util.stream.IntStream.range;
import static org.apache.commons.text.StringEscapeUtils.unescapeJava;

import static java.util.Objects.nonNull;

import io.vepo.kafka.load.exceptions.InvalidTestPlanException;
import io.vepo.kafka.load.parser.Connection;
import io.vepo.kafka.load.parser.PropertyValue;
import io.vepo.kafka.load.parser.TestPlan;
import io.vepo.kafka.load.parser.TestPlan.TestPlanBuilder;
import io.vepo.kafka.load.parser.generated.TestPlanBaseListener;
import io.vepo.kafka.load.parser.generated.TestPlanParser;
import java.util.Arrays;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class TestPlanCreator extends TestPlanBaseListener {

    private final TestPlanBuilder builder;
    private Connection.ConnectionBuilder connectionBuilder;

    public TestPlanCreator() {
        this.builder = TestPlan.builder();
    }

    @Override
    public void enterPlan(TestPlanParser.PlanContext ctx) {
        if (nonNull(ctx.IDENTIFIER())) {
            this.builder.name(ctx.IDENTIFIER().getText());
        } else {
            throw new InvalidTestPlanException("Test plan without identifier!");
        }
    }

    @Override
    public void enterConnection(TestPlanParser.ConnectionContext ctx) {
        this.connectionBuilder = Connection.builder();
    }

    @Override
    public void exitAttribute(TestPlanParser.AttributeContext ctx) {
        if (ctx.parent instanceof TestPlanParser.ConnectionContext) {
            switch (ctx.IDENTIFIER().getText()) {
                case "bootstrapServer":
                    if (nonNull(ctx.value())) {
                        if (nonNull(ctx.value().MULTILINE_STRING())) {
                            connectionBuilder.bootstrapServer(
                                    PropertyValue.fromText(processMultiLineString(ctx.value().getText())));
                        } else if (nonNull(ctx.value().STRING())) {
                            connectionBuilder
                                    .bootstrapServer(PropertyValue.fromText(processString(ctx.value().getText())));
                        }
                    } else if (nonNull(ctx.propertyReference())) {
                        connectionBuilder.bootstrapServer(
                                PropertyValue.fromReference(ctx.propertyReference().IDENTIFIER().getText()));
                    }
                    break;
            }
        }
    }

    private String processString(String text) {
        return unescapeJava(text.substring(1, text.length() - 1));
    }

    private String processMultiLineString(String text) {
        String[] lines = text.substring(3, text.length() - 3).split("\n");
        if (lines.length > 0) {

            if (lines[0].trim().isEmpty()) {
                lines = Arrays.copyOfRange(lines, 1, lines.length);
            }

            if (lines[lines.length - 1].trim().isEmpty()) {
                lines = Arrays.copyOfRange(lines, 0, lines.length - 1);
            }

            removeTabs(lines);
        }
        return Stream.of(lines).collect(Collectors.joining("\n"));
    }

    private static final Pattern LINE_START_PATTERN = Pattern.compile("^(\\s+)");

    private void removeTabs(String[] lines) {
        Matcher tabMatcher = LINE_START_PATTERN.matcher(lines[0]);
        if (tabMatcher.find()) {
            String tabPattern = tabMatcher.group(1);
            range(0, lines.length).filter(index -> lines[index].startsWith(tabPattern))
                    .forEach(index -> lines[index] = lines[index].substring(tabPattern.length()));
        }
    }

    @Override
    public void exitConnection(TestPlanParser.ConnectionContext ctx) {
        this.builder.connection(connectionBuilder.build());
    }

    public TestPlan buildTestPlan() {
        return this.builder.build();
    }
}
