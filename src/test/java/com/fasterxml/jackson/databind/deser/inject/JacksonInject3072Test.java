package com.fasterxml.jackson.databind.deser.inject;

import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.annotation.JacksonInject;
import com.fasterxml.jackson.annotation.OptBoolean;

import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.exc.InvalidDefinitionException;
import com.fasterxml.jackson.databind.testutil.DatabindTestUtil;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class JacksonInject3072Test extends DatabindTestUtil
{
    static class DtoWithOptional {
        @JacksonInject("id")
        String id;

        @JacksonInject(value = "optionalField", optional = OptBoolean.TRUE)
        String optionalField;

        public String getId() {
            return id;
        }

        public String getOptionalField() {
            return optionalField;
        }
    }

    static class DtoWithRequired {
        @JacksonInject(value = "requiredField", optional = OptBoolean.FALSE)
        public String requiredField;
    }

    private final ObjectReader READER = newJsonMapper().readerFor(DtoWithOptional.class);

    @Test
    void testOptionalFieldFound() throws Exception {
        ObjectReader reader = READER
                .with(new InjectableValues.Std()
                        .addValue("id", "idValue")
                        .addValue("optionalField", "optionalFieldValue"));

        DtoWithOptional dto = reader.readValue("{}");

        assertEquals("idValue", dto.id);
        assertEquals("optionalFieldValue", dto.optionalField);
    }

    @Test
    void testOptionalFieldNotFound() throws Exception {
        ObjectReader reader = READER
                .with(new InjectableValues.Std()
                        .addValue("id", "idValue"));

        DtoWithOptional dto = reader.readValue("{}");

        assertEquals("idValue", dto.id);
        assertNull(dto.optionalField);
    }

    @Test
    void testMandatoryFieldNotFound() {
        ObjectReader reader = READER;

        InvalidDefinitionException exception = assertThrows(
                InvalidDefinitionException.class, () -> reader.readValue("{}"));

        assertEquals("No 'injectableValues' configured, cannot inject value with id [id]\n" +
                " at [Source: (String)\"{}\"; line: 1, column: 2]", exception.getMessage());
    }

    @Test
    void testRequiredAnnotatedFieldNotFound() {
        // Should also fail even if DeserFeature disabled, if annotated
        ObjectReader reader = READER.forType(DtoWithRequired.class)
            .without(DeserializationFeature.FAIL_ON_UNKNOWN_INJECT_VALUE);

        InvalidDefinitionException exception = assertThrows(
                InvalidDefinitionException.class, () -> reader.readValue("{}"));

        assertEquals("No 'injectableValues' configured, cannot inject value with id [requiredField]\n" +
                " at [Source: (String)\"{}\"; line: 1, column: 2]", exception.getMessage());
    }

    @Test
    void testMandatoryFieldNotFoundWithInjectableValues() {
        ObjectReader reader = READER
                .with(new InjectableValues.Std());

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class, () -> reader.readValue("{}"));

        assertEquals("No injectable value with id 'id' found (for property 'id')",
                exception.getMessage());
    }

    @Test
    void testMandatoryFieldNotFoundWithoutDeserializationFeature() throws Exception {
        ObjectReader reader = READER
                .with(new InjectableValues.Std()
                        .addValue("id", "idValue"))
                .without(DeserializationFeature.FAIL_ON_UNKNOWN_INJECT_VALUE);

        DtoWithOptional dto = reader.readValue("{}");

        assertEquals("idValue", dto.id);
        assertNull(dto.optionalField);
    }

    @Test
    void testMandatoryFieldNotFoundWithInjectableValuesWithoutDeserializationFeature() throws Exception {
        ObjectReader reader = READER
                .with(new InjectableValues.Std())
                .without(DeserializationFeature.FAIL_ON_UNKNOWN_INJECT_VALUE);

        DtoWithOptional dto = reader.readValue("{}");

        assertNull(dto.id);
        assertNull(dto.optionalField);
    }

    @Test
    void testOptionalFieldNotFoundWithoutInjectableValuesWithDeserializationFeature() throws Exception {
        ObjectReader reader = READER
                .without(DeserializationFeature.FAIL_ON_UNKNOWN_INJECT_VALUE);

        DtoWithOptional dto = reader.readValue("{}");

        assertNull(dto.id);
        assertNull(dto.optionalField);
    }
}
