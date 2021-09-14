package io.vepo.kafka.load.parser;

public interface PropertyValue {

    public static PropertyValue fromText(String content) {
        return new PropertyStringValue(content);
    }

    public static PropertyValue fromReference(String property) {
        return new PropertyReferenceValue(property);
    }
}
