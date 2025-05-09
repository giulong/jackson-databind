package com.fasterxml.jackson.databind.deser.inject;

import com.fasterxml.jackson.annotation.JacksonInject;
import com.fasterxml.jackson.annotation.OptBoolean;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.exc.InvalidDefinitionException;
import com.fasterxml.jackson.databind.testutil.DatabindTestUtil;
import org.junit.jupiter.api.Test;

import static com.fasterxml.jackson.databind.DeserializationFeature.FAIL_ON_UNKNOWN_INJECT_VALUE;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class JacksonInject3072Test extends DatabindTestUtil {

    static class Dto {

        @JacksonInject("id")
        String id;

        @JacksonInject(value = "optionalField", optional = OptBoolean.TRUE)
        String optionalField;

        @SuppressWarnings("unused")
        public String getId() {
            return id;
        }

        @SuppressWarnings("unused")
        public String getOptionalField() {
            return optionalField;
        }
    }

    private ObjectReader newReader() {
        return newJsonMapper().readerFor(Dto.class);
    }

    @Test
    void testOptionalFieldFound() {
        ObjectReader reader = newReader()
                .with(new InjectableValues.Std()
                        .addValue("id", "idValue")
                        .addValue("optionalField", "optionalFieldValue"));

        Dto dto = assertDoesNotThrow(() -> reader.readValue("{}"));

        assertEquals("idValue", dto.id);
        assertEquals("optionalFieldValue", dto.optionalField);
    }

    @Test
    void testOptionalFieldNotFound() {
        ObjectReader reader = newReader()
                .with(new InjectableValues.Std()
                        .addValue("id", "idValue"));

        Dto dto = assertDoesNotThrow(() -> reader.readValue("{}"));

        assertEquals("idValue", dto.id);
        assertNull(dto.optionalField);
    }

    @Test
    void testMandatoryFieldNotFound() {
        ObjectReader reader = newReader();

        InvalidDefinitionException exception = assertThrows(
                InvalidDefinitionException.class, () -> reader.readValue("{}"));

        assertEquals("No 'injectableValues' configured, cannot inject value with id [id]\n" +
                " at [Source: (String)\"{}\"; line: 1, column: 2]", exception.getMessage());
    }

    @Test
    void testMandatoryFieldNotFoundWithInjectableValues() {
        ObjectReader reader = newReader()
                .with(new InjectableValues.Std());

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class, () -> reader.readValue("{}"));

        assertEquals("No injectable value with id 'id' found (for property 'id')",
                exception.getMessage());
    }

    @Test
    void testMandatoryFieldNotFoundWithoutDeserializationFeature() {
        ObjectReader reader = newReader()
                .with(new InjectableValues.Std()
                        .addValue("id", "idValue"))
                .without(FAIL_ON_UNKNOWN_INJECT_VALUE);

        Dto dto = assertDoesNotThrow(() -> reader.readValue("{}"));

        assertEquals("idValue", dto.id);
        assertNull(dto.optionalField);
    }

    @Test
    void testMandatoryFieldNotFoundWithInjectableValuesWithoutDeserializationFeature() {
        ObjectReader reader = newReader()
                .with(new InjectableValues.Std())
                .without(FAIL_ON_UNKNOWN_INJECT_VALUE);

        Dto dto = assertDoesNotThrow(() -> reader.readValue("{}"));

        assertNull(dto.id);
        assertNull(dto.optionalField);
    }

    @Test
    void testOptionalFieldNotFoundWithoutInjectableValuesWithDeserializationFeature() {
        ObjectReader reader = newReader()
                .without(FAIL_ON_UNKNOWN_INJECT_VALUE);

        Dto dto = assertDoesNotThrow(() -> reader.readValue("{}"));

        assertNull(dto.id);
        assertNull(dto.optionalField);
    }
}
