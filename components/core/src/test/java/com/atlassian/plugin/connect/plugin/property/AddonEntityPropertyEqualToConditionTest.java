package com.atlassian.plugin.connect.plugin.property;

import com.atlassian.fugue.Option;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

import org.apache.commons.lang3.StringUtils;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.node.JsonNodeFactory;
import org.codehaus.jackson.node.ObjectNode;
import org.junit.Test;

import static com.atlassian.plugin.connect.plugin.property.AddonEntityPropertyEqualToCondition.getValueForPath;
import static org.junit.Assert.*;

public class AddonEntityPropertyEqualToConditionTest
{
    public static final String COMPLICATED_EXAMPLE = "{\"one\": {\"two\": {\"three\": true, \"ignore\": \"this\"}, \"ignore\": \"this\" }, \"random\": \"example\"}";

    private static JsonNode getComplicatedExample() {
        final ObjectNode root = JsonNodeFactory.instance.objectNode();
    }

    @Test
    public void testGetValueForPath__null_json_entity_returns_nothing() {
        assertFalse(getValueForPath(null, "key").isPresent());
    }

    @Test(expected = NullPointerException.class)
    public void testGetValueForPath__null_object_name_throws_exception() {
        final ObjectNode emptyObject = JsonNodeFactory.instance.objectNode();
        getValueForPath(emptyObject, null);
    }

    @Test
    public void testGetValueForPath__no_object_name_returns_equivalent_object() {
        final JsonElement inputJson = new JsonParser().parse(COMPLICATED_EXAMPLE);
        final Option<JsonElement> result = getValueForPath(inputJson, StringUtils.EMPTY);
        final JsonElement expectedResult = new JsonParser().parse(COMPLICATED_EXAMPLE);
        assertEquals(Option.some(expectedResult), result);
    }

    @Test
    public void testGetValueForPath__one_level_deep() {
        final JsonElement inputJson = new JsonParser().parse(COMPLICATED_EXAMPLE);
        final Option<JsonElement> result = getValueForPath(inputJson, "one");
        final JsonElement expectedResult = new JsonParser().parse("{\"two\": {\"three\": true, \"ignore\": \"this\"}, \"ignore\": \"this\" }");
        assertEquals(Option.some(expectedResult), result);
    }

    @Test
    public void testGetValueForPath__two_levels_deep() {
        final JsonElement inputJson = new JsonParser().parse(COMPLICATED_EXAMPLE);
        final Option<JsonElement> result = getValueForPath(inputJson, "one.two");
        final JsonElement expectedResult = new JsonParser().parse("{\"three\": true, \"ignore\": \"this\"}");
        assertEquals(Option.some(expectedResult), result);
    }

    @Test
    public void testGetValueForPath__three_levels_deep() {
        final JsonElement inputJson = new JsonParser().parse(COMPLICATED_EXAMPLE);
        final Option<JsonElement> result = getValueForPath(inputJson, "one.two.three");
        final JsonElement expectedResult = new JsonParser().parse("true");
        assertEquals(Option.some(expectedResult), result);
    }

    @Test
    public void testGetValueForPath__missing_object_in_path() {
        final JsonElement inputJson = new JsonParser().parse(COMPLICATED_EXAMPLE);
        final Option<JsonElement> result = getValueForPath(inputJson, "one.none");
        assertEquals(Option.none(), result);
    }

    @Test
    public void testGetValueForPath__missing_object_in_complex_path() {
        final JsonElement inputJson = new JsonParser().parse(COMPLICATED_EXAMPLE);
        final Option<JsonElement> result = getValueForPath(inputJson, "one.none.three");
        assertEquals(Option.none(), result);
    }

    @Test
    public void testGetValueForPath__primitive_in_path() {
        final JsonElement inputJson = new JsonParser().parse("{\"one\": {\"two\": true, \"ignore\": \"this\" }, \"random\": \"example\"}");
        final Option<JsonElement> result = getValueForPath(inputJson, "one.two.three");
        assertEquals(Option.none(), result);
    }

    @Test
    public void testGetValueForPath__array_in_path() {
        // one.two would return an array with three in it. Thus there is no path to three
        final JsonElement inputJson = new JsonParser().parse("{\"one\": {\"two\": [{\"three\": true, \"ignore\": \"this\"}], \"ignore\": \"this\" }, \"random\": \"example\"}");
        final Option<JsonElement> result = getValueForPath(inputJson, "one.two.three");
        assertEquals(Option.none(), result);
    }
}