package io.vepo.kafka.load.parser;

import java.text.NumberFormat;
import java.text.ParseException;

public interface PropertyValue {

    class PropertyNullValue implements PropertyValue {
        private PropertyNullValue() {
        }
    }

    PropertyNullValue NULL = new PropertyNullValue();

    static PropertyValue fromText(String content) {
        return new PropertyStringValue(content);
    }

    static PropertyValue fromReference(String property) {
        return new PropertyReferenceValue(property);
    }

    static PropertyValue fromNumber(String value) {
        try {
            return new PropertyNumberValue(NumberFormat.getInstance().parse(value));
        } catch (ParseException pe) {
            throw new IllegalStateException("Invalid number! value=" + value);
        }
    }


}
