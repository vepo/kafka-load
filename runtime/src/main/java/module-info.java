module io.vepo.kafka.load.runtime {
    requires info.picocli;
    requires io.vepo.kafka.load.engine;
    requires io.vepo.kafka.load.parser;

    opens io.vepo.kafka.load.runtime to info.picocli;
}