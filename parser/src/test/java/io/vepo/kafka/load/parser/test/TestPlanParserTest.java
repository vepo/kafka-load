package io.vepo.kafka.load.parser.test;

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
import java.time.Duration;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("Test Plan Parser")
class TestPlanParserTest {

    @Test
    @DisplayName("Simple Message Sender")
    void parseSendMessage() {
        assertEquals(TestPlan.builder()
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
                        .build(),
                TestPlanFactory.parse("""
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
                        """));
    }
}