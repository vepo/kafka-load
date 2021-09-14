package io.vepo.kafka.load.parser.internal;

import static java.util.Objects.nonNull;
import static java.util.stream.IntStream.range;
import static org.apache.commons.text.StringEscapeUtils.unescapeJava;

import io.vepo.kafka.load.exceptions.InvalidTestPlanException;
import io.vepo.kafka.load.parser.Connection;
import io.vepo.kafka.load.parser.TestPlan;
import io.vepo.kafka.load.parser.generated.TestPlanBaseListener;
import io.vepo.kafka.load.parser.generated.TestPlanParser;
import java.time.Duration;
import java.util.Arrays;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import io.vepo.kafka.load.parser.PropertyValue;

public class TestPlanCreator extends TestPlanBaseListener {

    private static final Pattern LINE_START_PATTERN = Pattern.compile("^(\\s+)");

    private static final Pattern TIME_VALUE = Pattern.compile("([0-9]+)([a-z]+)");

    private static int timeUnitInMillis(String timeUnit) {
        return switch (timeUnit) {
            case "ms" -> 1;
            case "s" -> 1000;
            case "m" -> 60_000;
            case "h" -> 3_600_000;
            default -> throw new IllegalStateException("Time unit not implemented: timeUnit=" + timeUnit);
        };
    }

    private static <T> void applyDurationValue(TestPlanParser.ValueContext valueContext, Function<Duration, T> fn) {
        if (nonNull(fn)) {
            var matcher = TIME_VALUE.matcher(valueContext.TIME_VALUE().getText());
            if (matcher.matches()) {
                fn.apply(Duration.ofMillis(Integer.parseInt(matcher.group(1)) * timeUnitInMillis(matcher.group(2))));
            } else {
                throw new IllegalStateException("Wrong time value!");
            }
        }
    }

    private static <T> void applyNumberValue(TestPlanParser.ValueContext valueContext, Function<Integer, T> fn) {
        if (nonNull(valueContext.NUMBER())) {
            if (nonNull(fn)) {
                fn.apply(Integer.parseInt(valueContext.NUMBER().getText()));
            }
        }
    }

    private final TestPlan.TestPlanBuilder builder;

    private Connection.ConnectionBuilder connectionBuilder;

    public TestPlanCreator() {
        this.builder = TestPlan.builder();
    }

    public TestPlan buildTestPlan() {
        return this.builder.build();
    }

    @Override
    public void enterConnection(TestPlanParser.ConnectionContext ctx) {
        this.connectionBuilder = Connection.builder();
    }

    @Override
    public void enterPlan(TestPlanParser.PlanContext ctx) {
        if (!nonNull(ctx.IDENTIFIER())) {
            throw new InvalidTestPlanException("Test plan without identifier!");
        }
        this.builder.name(ctx.IDENTIFIER().getText());
    }

    @Override
    public void exitAttribute(TestPlanParser.AttributeContext ctx) {
        if (ctx.parent instanceof TestPlanParser.PlanContext) {
            if (nonNull(ctx.value().TIME_VALUE())) {
                applyDurationValue(ctx.value(), switch (ctx.IDENTIFIER().getText()) {
                    case "cycleTime" -> this.builder::cycleTime;
                    case "execution" -> this.builder::execution;
                    case "warmUp" -> this.builder::warmUp;
                    case "rampDown" -> this.builder::rampDown;
                    default -> null;
                });
            }
            applyNumberValue(ctx.value(), switch (ctx.IDENTIFIER().getText()) {
                case "clients" -> this.builder::clients;
                default -> null;
            });
        } else if (ctx.parent instanceof TestPlanParser.ConnectionContext) {
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

    @Override
    public void exitConnection(TestPlanParser.ConnectionContext ctx) {
        this.builder.connection(connectionBuilder.build());
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

    private String processString(String text) {
        return unescapeJava(text.substring(1, text.length() - 1));
    }

    private void removeTabs(String[] lines) {
        Matcher tabMatcher = LINE_START_PATTERN.matcher(lines[0]);
        if (tabMatcher.find()) {
            String tabPattern = tabMatcher.group(1);
            range(0, lines.length).filter(index -> lines[index].startsWith(tabPattern))
                    .forEach(index -> lines[index] = lines[index].substring(tabPattern.length()));
        }
    }
}
