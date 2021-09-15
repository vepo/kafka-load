package io.vepo.kafka.load.parser.internal;

import static io.vepo.kafka.load.parser.PropertyValue.NULL;
import static java.util.Objects.nonNull;
import static java.util.stream.IntStream.range;
import static org.apache.commons.text.StringEscapeUtils.unescapeJava;

import io.vepo.kafka.load.parser.Assertion;
import io.vepo.kafka.load.parser.Connection;
import io.vepo.kafka.load.parser.Message;
import io.vepo.kafka.load.parser.MessageAssertion;
import io.vepo.kafka.load.parser.MessageType;
import io.vepo.kafka.load.parser.Operator;
import io.vepo.kafka.load.parser.Step;
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
    private final TestPlan.TestPlanBuilder testPlanBuilder;
    private Connection.ConnectionBuilder connectionBuilder;
    private Step.StepBuilder stepBuilder;
    private Message.MessageBuilder messageBuilder;
    private Assertion.AssertionBuilder assertionBuilder;

    public TestPlanCreator() {
        testPlanBuilder = TestPlan.builder();
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

    @Override
    public void enterAssertion(TestPlanParser.AssertionContext ctx) {
        assertionBuilder = Assertion.builder();
    }

    @Override
    public void exitAssertion(TestPlanParser.AssertionContext ctx) {
        stepBuilder.assertion(assertionBuilder.build());
    }

    public TestPlan buildTestPlan() {
        return testPlanBuilder.build();
    }

    @Override
    public void enterConnection(TestPlanParser.ConnectionContext ctx) {
        connectionBuilder = Connection.builder();
    }

    @Override
    public void enterPlan(TestPlanParser.PlanContext ctx) {
        if (!nonNull(ctx.IDENTIFIER())) {
            throw new InvalidTestPlanException("Test plan without identifier!");
        }
        testPlanBuilder.name(ctx.IDENTIFIER().getText());
    }

    @Override
    public void exitAttribute(TestPlanParser.AttributeContext ctx) {
        if (ctx.parent instanceof TestPlanParser.PlanContext) {
            if (nonNull(ctx.value().TIME_VALUE())) {
                applyDurationValue(ctx.value(), switch (ctx.IDENTIFIER().getText()) {
                    case "cycleTime" -> testPlanBuilder::cycleTime;
                    case "execution" -> testPlanBuilder::execution;
                    case "warmUp" -> testPlanBuilder::warmUp;
                    case "rampDown" -> testPlanBuilder::rampDown;
                    default -> null;
                });
            } else if (nonNull(ctx.value().NUMBER())) {
                applyNumberValue(ctx.value(), switch (ctx.IDENTIFIER().getText()) {
                    case "clients" -> testPlanBuilder::clients;
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
        } else if (ctx.parent instanceof TestPlanParser.MessageContext) {
            applyStringValue(ctx, switch (ctx.IDENTIFIER().getText()) {
                case "topic" -> messageBuilder::topic;
                case "key" -> messageBuilder::key;
                case "value" -> messageBuilder::value;
                default -> null;
            });
        } else if (ctx.parent instanceof TestPlanParser.AssertionContext) {
            applyStringValue(ctx, switch (ctx.IDENTIFIER().getText()) {
                case "topic" -> assertionBuilder::topic;
                default -> null;
            });
        }
    }

    @Override
    public void exitMessageAssertion(TestPlanParser.MessageAssertionContext ctx) {
        assertionBuilder.assertion(MessageAssertion.builder()
                .path(ctx.JSON_PATH().getText())
                .operator(switch (ctx.OPERATOR().getText()) {
                    case "==" -> Operator.EQUALS;
                    default -> throw new IllegalStateException(
                            "Operator not implemented! operator=" + ctx.OPERATOR().getText());
                })
                .value(extractAssertionValue(ctx))
                .build());
    }

    private PropertyValue extractAssertionValue(TestPlanParser.MessageAssertionContext ctx) {
        if (nonNull(ctx.messageAssertionValue())) {
            if (nonNull(ctx.messageAssertionValue().NUMBER())) {
                return PropertyValue.fromNumber(ctx.messageAssertionValue().NUMBER().getText());
            } else if (nonNull(ctx.messageAssertionValue().MULTILINE_STRING())) {
                return PropertyValue.fromText(processMultiLineString(ctx.messageAssertionValue().getText()));
            } else if (nonNull(ctx.messageAssertionValue().STRING())) {
                return PropertyValue.fromText(processString(ctx.messageAssertionValue().getText()));
            } else if (nonNull(ctx.messageAssertionValue().NULL())) {
                return NULL;
            }
        } else if (nonNull(ctx.propertyReference())) {
            return PropertyValue.fromReference(ctx.propertyReference().IDENTIFIER().getText());
        }
        throw new IllegalStateException("Type not implemented yet!");
    }

    @Override
    public void enterStep(TestPlanParser.StepContext ctx) {
        stepBuilder = Step.builder().name(ctx.IDENTIFIER().getText());
    }

    @Override
    public void enterMessage(TestPlanParser.MessageContext ctx) {
        messageBuilder = Message.builder();
    }

    @Override
    public void exitMessage(TestPlanParser.MessageContext ctx) {
        stepBuilder.message(messageBuilder.build());
    }

    @Override
    public void exitStep(TestPlanParser.StepContext ctx) {
        testPlanBuilder.step(stepBuilder.build());
        stepBuilder = null;
    }

    @Override
    public void exitConnection(TestPlanParser.ConnectionContext ctx) {
        testPlanBuilder.connection(connectionBuilder.build());
        connectionBuilder = null;
    }
}
