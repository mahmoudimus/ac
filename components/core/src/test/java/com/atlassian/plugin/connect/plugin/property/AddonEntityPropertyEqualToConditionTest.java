package com.atlassian.plugin.connect.plugin.property;

import java.io.IOException;
import java.util.Optional;

import org.apache.commons.lang3.StringUtils;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.node.ArrayNode;
import org.codehaus.jackson.node.JsonNodeFactory;
import org.codehaus.jackson.node.ObjectNode;
import org.junit.Test;

import static com.atlassian.plugin.connect.plugin.property.AddonEntityPropertyEqualToCondition.getValueForPath;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

public class AddonEntityPropertyEqualToConditionTest
{
    public static final JsonNode COMPLICATED_EXAMPLE, LEVEL_ONE, LEVEL_TWO;

    static {
        final JsonNode root = getComplicatedExample();

        COMPLICATED_EXAMPLE = root;
        LEVEL_ONE = root.get("one");
        LEVEL_TWO = LEVEL_ONE.get("two");
    }

    private static JsonNode getComplicatedExample() {
        final ObjectNode root = JsonNodeFactory.instance.objectNode();
        root.put("random", "example");

        final ObjectNode one = root.putObject("one");
        one.put("ignore", "this");

        final ObjectNode two = one.putObject("two");
        two.put("three", true);
        two.put("ignore", "this");

        return root;
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
    public void testGetValueForPath__no_object_name_returns_equivalent_object() throws IOException
    {
        final Optional<JsonNode> result = getValueForPath(getComplicatedExample(), StringUtils.EMPTY);
        assertEquals(Optional.of(COMPLICATED_EXAMPLE), result);
    }

    @Test
    public void testGetValueForPath__one_level_deep() {
        final Optional<JsonNode> result = getValueForPath(getComplicatedExample(), "one");
        assertEquals(Optional.of(LEVEL_ONE), result);
    }

    @Test
    public void testGetValueForPath__two_levels_deep() {
        final Optional<JsonNode> result = getValueForPath(getComplicatedExample(), "one.two");
        assertEquals(Optional.of(LEVEL_TWO), result);
    }

    @Test
    public void testGetValueForPath__three_levels_deep() {
        final Optional<JsonNode> result = getValueForPath(getComplicatedExample(), "one.two.three");
        final JsonNode expectedResult = JsonNodeFactory.instance.booleanNode(true);
        assertEquals(Optional.of(expectedResult), result);
    }

    @Test
    public void testGetValueForPath__missing_object_in_path() {
        final Optional<JsonNode> result = getValueForPath(getComplicatedExample(), "one.none");
        assertEquals(Optional.empty(), result);
    }

    @Test
    public void testGetValueForPath__missing_object_in_complex_path() {
        final Optional<JsonNode> result = getValueForPath(getComplicatedExample(), "one.none.three");
        assertEquals(Optional.empty(), result);
    }

    @Test
    public void testGetValueForPath__primitive_in_path() {
        final ObjectNode root = JsonNodeFactory.instance.objectNode();
        root.put("random", "example");

        final ObjectNode one = root.putObject("one");
        one.put("two", "true");
        one.put("ignore", "this");

        final Optional<JsonNode> result = getValueForPath(root, "one.two.three");
        assertEquals(Optional.empty(), result);
    }

    @Test
    public void testGetValueForPath__array_in_path() {
        // one.two would return an array with three in it. Thus there is no path to three
        final ObjectNode root = JsonNodeFactory.instance.objectNode();
        root.put("random", "example");

        final ObjectNode one = root.putObject("one");
        one.put("ignore", "this");

        // This sneaky array is what makes this not resolve
        final ArrayNode twoArr = one.putArray("two");
        final ObjectNode two = twoArr.addObject();
        two.put("three", true);
        two.put("ignore", "this");

        final Optional<JsonNode> result = getValueForPath(root, "one.two.three");
        assertEquals(Optional.empty(), result);
    }
}