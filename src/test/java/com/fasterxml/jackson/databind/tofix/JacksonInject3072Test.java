package com.fasterxml.jackson.databind.tofix;

import com.fasterxml.jackson.annotation.JacksonInject;
import com.fasterxml.jackson.annotation.OptBoolean;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.exc.InvalidDefinitionException;
import com.fasterxml.jackson.databind.testutil.DatabindTestUtil;
import org.junit.jupiter.api.Test;

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

    @Test
    void testOptionalFieldFound() {
        ObjectReader reader = newJsonMapper()
                .readerFor(Dto.class)
                .with(new InjectableValues.Std()
                        .addValue("id", "idValue")
                        .addValue("optionalField", "optionalFieldValue"));

        Dto dto = assertDoesNotThrow(() -> reader.readValue("{}"));

        assertEquals("idValue", dto.id);
        assertEquals("optionalFieldValue", dto.optionalField);
    }

    @Test
    void testOptionalFieldNotFound() {
        ObjectReader reader = newJsonMapper()
                .readerFor(Dto.class)
                .with(new InjectableValues.Std()
                        .addValue("id", "idValue"));

        Dto dto = assertDoesNotThrow(() -> reader.readValue("{}"));

        assertEquals("idValue", dto.id);
        assertNull(dto.optionalField);
    }

    @Test
    void testMandatoryFieldNotFound() {
        ObjectReader reader = newJsonMapper()
                .readerFor(Dto.class);

        InvalidDefinitionException exception = assertThrows(
                InvalidDefinitionException.class, () -> reader.readValue("{}"));

        assertEquals("No 'injectableValues' configured, cannot inject value with id [id]\n" +
                " at [Source: (String)\"{}\"; line: 1, column: 2]", exception.getMessage());
    }

    @Test
    void testMandatoryFieldNotFoundWithInjectableValues() {
        ObjectReader reader = newJsonMapper()
                .readerFor(Dto.class)
                .with(new InjectableValues.Std());

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class, () -> reader.readValue("{}"));

        assertEquals("No injectable id with value 'id' found (for property 'id')",
                exception.getMessage());
    }
}
