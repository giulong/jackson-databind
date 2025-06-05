package com.fasterxml.jackson.databind.deser.inject;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.annotation.JacksonInject;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.OptBoolean;

import com.fasterxml.jackson.databind.InjectableValues;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.exc.MissingInjectableValueExcepion;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.databind.testutil.DatabindTestUtil;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class JacksonInject1381Test extends DatabindTestUtil
{
    static class InputDefault
    {
        @JacksonInject(value = "key")
        private final String field;

        @JsonCreator
        public InputDefault(@JsonProperty("field") final String field) {
            this.field = field;
        }

        public String getField() {
            return field;
        }
    }

    static class InputTrue
    {
        @JacksonInject(value = "key", useInput = OptBoolean.TRUE)
        private final String field;

        @JsonCreator
        public InputTrue(@JsonProperty("field") final String field) {
            this.field = field;
        }

        public String getField() {
            return field;
        }
    }

    static class InputFalse
    {
        @JacksonInject(value = "key", useInput = OptBoolean.FALSE)
        private final String field;

        @JsonCreator
        public InputFalse(@JsonProperty("field") final String field) {
            this.field = field;
        }

        public String getField() {
            return field;
        }
    }

    private final String empty = "{}";
    private final String input = "{\"field\": \"input\"}";

    private final ObjectMapper plainMapper = newJsonMapper();
    private final ObjectMapper injectedMapper = jsonMapperBuilder()
            .injectableValues(new InjectableValues.Std().addValue("key", "injected"))
            .build();

    @Test
    @DisplayName("input NO, injectable NO, useInput DEFAULT|TRUE|FALSE => exception")
    void test1() {
        assertThrows(MissingInjectableValueExcepion.class,
                () -> plainMapper.readValue(empty, InputDefault.class));

        assertThrows(MissingInjectableValueExcepion.class,
                () -> plainMapper.readValue(empty, InputTrue.class));

        assertThrows(MissingInjectableValueExcepion.class,
                () -> plainMapper.readValue(empty, InputFalse.class));
    }

    @Test
    @DisplayName("input NO, injectable YES, useInput DEFAULT|TRUE|FALSE => injected")
    void test2() throws Exception {
        assertEquals("injected", injectedMapper.readValue(empty, InputDefault.class).getField());
        assertEquals("injected", injectedMapper.readValue(empty, InputTrue.class).getField());
        assertEquals("injected", injectedMapper.readValue(empty, InputFalse.class).getField());
    }

    @Test
    @DisplayName("input YES, injectable NO, useInput DEFAULT|FALSE => exception")
    void test3() {
        assertThrows(MissingInjectableValueExcepion.class,
                () -> plainMapper.readValue(input, InputDefault.class));

        assertThrows(MissingInjectableValueExcepion.class,
                () -> plainMapper.readValue(input, InputFalse.class));
    }

    @Test
    @DisplayName("input YES, injectable NO, useInput TRUE => input")
    void test4() throws Exception {
        assertEquals("input", plainMapper.readValue(input, InputTrue.class).getField());
    }

    @Test
    @DisplayName("input YES, injectable YES, useInput DEFAULT|FALSE => injected")
    void test5() throws Exception {
        assertEquals("injected", injectedMapper.readValue(input, InputDefault.class).getField());
        assertEquals("injected", injectedMapper.readValue(input, InputFalse.class).getField());
    }

    @Test
    @DisplayName("input YES, injectable YES, useInput TRUE => input")
    void test6() throws Exception {
        assertEquals("input", injectedMapper.readValue(input, InputTrue.class).getField());
    }
}
