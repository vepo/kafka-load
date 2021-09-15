package io.vepo.kafka.load.parser.test;

import static io.vepo.kafka.load.parser.TestPlanFactory.parseTestPlan;
import static org.junit.jupiter.api.Assertions.*;

import io.vepo.kafka.load.parser.Assertion;
import io.vepo.kafka.load.parser.Connection;
import io.vepo.kafka.load.parser.Message;
import io.vepo.kafka.load.parser.MessageAssertion;
import io.vepo.kafka.load.parser.MessageType;
import io.vepo.kafka.load.parser.Operator;
import io.vepo.kafka.load.parser.PropertyValue;
import io.vepo.kafka.load.parser.Step;
import io.vepo.kafka.load.parser.TestPlan;
import io.vepo.kafka.load.parser.TestPlanFactory;
import io.vepo.kafka.load.parser.exceptions.InvalidTestPlanException;
import java.security.InvalidParameterException;
import java.time.Duration;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

@DisplayName("Test Plan Parser")
class TestPlanParserTest {

    @Nested
    @DisplayName("Errors")
    class ErrorTest {
        @Test
        @DisplayName("Mo connection.bootstrapServer ")
        void missingBoostrapServerTest() {
            assertThrows(
                    InvalidTestPlanException.class,
                    () -> parseTestPlan("""
                            TestPlan Test1 {

                                connection {
                                }

                                Step1 {
                                    message {
                                        topic: "topic-1"
                                        key:   ${index} 
                                        value: ""\"
                                                {
                                                    "key": "value",
                                                    "index": "${index}"
                                                }
                                                ""\"
                                    }
                                }
                            }
                            """),
                    "Missing \"bootstrapServer\" on connection!");
        }

        @Test
        @DisplayName("No step defined")
        void missingStepTest() {
            assertThrows(
                    InvalidTestPlanException.class,
                    () -> parseTestPlan("""
                            TestPlan Test1 {

                                connection {
                                    bootstrapServer: "kafka:9092"
                                }
                            }
                            """),
                    "No Step defined! You should define at least one step.");
        }
    }


    @Test
    @DisplayName("Simple Message Sender with all fields")
    void parseSendMessageWithAllFieldsTest() {
        var expected = TestPlan.builder()
                .name("Test1")
                .clients(25)
                .cycleTime(Duration.ofSeconds(1))
                .warmUp(Duration.ofSeconds(60))
                .execution(Duration.ofSeconds(60))
                .rampDown(Duration.ofSeconds(60))
                .connection(Connection.builder()
                        .bootstrapServer(PropertyValue.fromText("kafka:9092"))
                        .produces(MessageType.JSON)
                        .consumes(MessageType.JSON)
                        .build())
                .step(Step.builder()
                        .name("Step1")
                        .message(Message.builder()
                                .topic(PropertyValue.fromText("topic-1"))
                                .key(PropertyValue.fromReference("index"))
                                .value(PropertyValue.fromText("""
                                        {
                                            "key": "value",
                                            "index": "${index}"
                                        }
                                        """))
                                .build())
                        .assertion(Assertion.builder()
                                .topic(PropertyValue.fromText("topic-1"))
                                .assertion(MessageAssertion.builder()
                                        .path("$.value.key")
                                        .operator(Operator.EQUALS)
                                        .value(PropertyValue.fromText("value"))
                                        .build())
                                .build())
                        .build())
                .build();
        var actual = parseTestPlan("""
                TestPlan Test1 {
                    clients:   25
                    cycleTime: 1s
                    warmUp:    60s
                    execution: 60s
                    rampDown:  60s

                    connection {
                        bootstrapServer: "kafka:9092"
                        produces: JSON
                        consumes: JSON
                    }

                    Step1 {
                        message {
                            topic: "topic-1"
                            key:   ${index} 
                            value: ""\"
                                    {
                                        "key": "value",
                                        "index": "${index}"
                                    }
                                    ""\"
                        }
                        assertion {
                            topic: "topic-1"
                            $.value.key == "value"
                        }
                    }
                }
                """);
        assertEquals(expected, actual);
    }

    @Test
    @DisplayName("Simple Message Sender with only required fields")
    void parseSendMessageOnlyRequiredFieldsTest() {
        var expected = TestPlan.builder()
                .name("Test1")
                .connection(Connection.builder()
                        .bootstrapServer(PropertyValue.fromText("kafka:9092"))
                        .build())
                .step(Step.builder()
                        .name("Step1")
                        .message(Message.builder()
                                .topic(PropertyValue.fromText("topic-1"))
                                .key(PropertyValue.fromReference("index"))
                                .value(PropertyValue.fromText("""
                                        {
                                            "key": "value",
                                            "index": "${index}"
                                        }
                                        """))
                                .build())
                        .build())
                .build();
        var actual = parseTestPlan("""
                TestPlan Test1 {

                    connection {
                        bootstrapServer: "kafka:9092"
                    }

                    Step1 {
                        message {
                            topic: "topic-1"
                            key:   ${index} 
                            value: ""\"
                                    {
                                        "key": "value",
                                        "index": "${index}"
                                    }
                                    ""\"
                        }
                    }
                }
                """);
        assertEquals(expected, actual);
    }
}