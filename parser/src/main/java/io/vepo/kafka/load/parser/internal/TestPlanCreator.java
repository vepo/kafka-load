package io.vepo.kafka.load.parser.internal;

import static java.util.Objects.nonNull;
import static java.util.stream.IntStream.range;
import static org.apache.commons.text.StringEscapeUtils.unescapeJava;

import io.vepo.kafka.load.parser.Connection;
import io.vepo.kafka.load.parser.MessageType;
import io.vepo.kafka.load.parser.TestPlan;
import io.vepo.kafka.load.parser.exceptions.InvalidTestPlanException;
import io.vepo.kafka.load.parser.generated.TestPlanBaseListener;
import io.vepo.kafka.load.parser.generated.TestPlanParser;
import java.time.Duration;
import java.util.Arrays;
import java.util.function.Function;
import java.util.regex.Pattern;

import io.vepo.kafka.load.parser.PropertyValue;

public class TestPlanCreator extends TestPlanBaseListener {

    private static final Pattern LINE_START_PATTERN = Pattern.compile("^(\\s+)");

    private static final Pattern TIME_VALUE = Pattern.compile("([0-9]+)([a-z]+)");
    private final TestPlan.TestPlanBuilder builder;
    private Connection.ConnectionBuilder connectionBuilder;

    public TestPlanCreator() {
        this.builder = TestPlan.builder();
    }

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
                fn.apply(Duration.ofMillis(Long.parseLong(matcher.group(1)) * timeUnitInMillis(matcher.group(2))));
            } else {
                throw new IllegalStateException("Wrong time value!");
            }
        }
    }

    private static <T> void applyNumberValue(TestPlanParser.ValueContext valueContext, Function<Integer, T> fn) {
        if (nonNull(fn)) {
            fn.apply(Integer.parseInt(valueContext.NUMBER().getText()));
        }
    }

    private static boolean isEnum(TestPlanParser.AttributeContext attributeContext) {
        return nonNull(attributeContext.value()) && nonNull(attributeContext.value().ENUM_VALUE());
    }

    private static <T, E extends Enum> void applyEnumValue(TestPlanParser.AttributeContext attributeContext,
                                                           Function<E, T> fn,
                                                           Class<E> enumClass) {
        if (nonNull(fn)) {
            fn.apply((E) Enum.valueOf(enumClass, attributeContext.value().ENUM_VALUE().getText()));
        }
    }

    private static <T> void applyStringValue(TestPlanParser.AttributeContext attributeContext,
                                             Function<PropertyValue, T> fn) {
        if (nonNull(fn)) {
            if (nonNull(attributeContext.value())) {
                if (nonNull(attributeContext.value().MULTILINE_STRING())) {
                    fn.apply(PropertyValue.fromText(processMultiLineString(attributeContext.value().getText())));
                } else if (nonNull(attributeContext.value().STRING())) {
                    fn.apply(PropertyValue.fromText(processString(attributeContext.value().getText())));
                }
            } else if (nonNull(attributeContext.propertyReference())) {
                fn.apply(PropertyValue.fromReference(attributeContext.propertyReference().IDENTIFIER().getText()));
            }
        }
    }

    private static String processMultiLineString(String text) {
        var lines = text.substring(3, text.length() - 3).split("\n");
        if (lines.length > 0) {

            if (lines[0].trim().isEmpty()) {
                lines = Arrays.copyOfRange(lines, 1, lines.length);
            }

            if (lines[lines.length - 1].trim().isEmpty()) {
                lines = Arrays.copyOfRange(lines, 0, lines.length - 1);
            }

            removeTabs(lines);
        }
        return String.join("\n", lines);
    }

    private static String processString(String text) {
        return unescapeJava(text.substring(1, text.length() - 1));
    }

    private static void removeTabs(String[] lines) {
        var tabMatcher = LINE_START_PATTERN.matcher(lines[0]);
        if (tabMatcher.find()) {
            var tabPattern = tabMatcher.group(1);
            range(0, lines.length).filter(index -> lines[index].startsWith(tabPattern))
                    .forEach(index -> lines[index] = lines[index].substring(tabPattern.length()));
        }
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
            } else if (nonNull(ctx.value().NUMBER())) {
                applyNumberValue(ctx.value(), switch (ctx.IDENTIFIER().getText()) {
                    case "clients" -> this.builder::clients;
                    default -> null;
                });
            }
        } else if (ctx.parent instanceof TestPlanParser.ConnectionContext) {
            if (isEnum(ctx)) {
                applyEnumValue(ctx, switch (ctx.IDENTIFIER().getText()) {
                    case "produces" -> connectionBuilder::produces;
                    case "consumes" -> connectionBuilder::consumes;
                    default -> null;
                }, MessageType.class);
            } else {
                applyStringValue(ctx, switch (ctx.IDENTIFIER().getText()) {
                    case "bootstrapServer" -> connectionBuilder::bootstrapServer;
                    default -> null;
                });
            }
        }
    }

    @Override
    public void exitConnection(TestPlanParser.ConnectionContext ctx) {
        this.builder.connection(connectionBuilder.build());
    }
}
