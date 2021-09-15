package io.vepo.kafka.load.parser;

public interface PropertyValue {

    static PropertyValue fromText(String content) {
        return new PropertyStringValue(content);
    }

    static PropertyValue fromReference(String property) {
        return new PropertyReferenceValue(property);
    }

}
