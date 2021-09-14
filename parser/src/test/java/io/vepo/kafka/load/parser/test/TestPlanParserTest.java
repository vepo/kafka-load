package io.vepo.kafka.load.parser.test;

import static org.junit.jupiter.api.Assertions.*;

import io.vepo.kafka.load.parser.Connection;
import io.vepo.kafka.load.parser.PropertyValue;
import io.vepo.kafka.load.parser.TestPlan;
import io.vepo.kafka.load.parser.TestPlanFactory;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("Test Plan Parser")
class TestPlanParserTest {

    @Test
    @DisplayName("Simple Message Sender")
    void parseSendMessage() {
        assertEquals(TestPlan.builder()
                        .name("Test1")
                        .connection(Connection.builder()
                                .bootstrapServer(PropertyValue.fromText("kafka:9092"))
                                .build())
                        .build(),
                TestPlanFactory.parse("""
                        TestPlan Test1 {
                            connection {
                                bootstrapServer: "kafka:9092"
                            }
                        }
                        """));
    }
}