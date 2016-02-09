package com.atlassian.plugin.connect.plugin.property;

import java.io.IOException;
import java.sql.Timestamp;
import java.util.Optional;

import com.atlassian.fugue.Option;
import com.atlassian.plugin.PluginParseException;
import com.atlassian.plugin.connect.api.property.AddonProperty;
import com.atlassian.plugin.connect.api.property.AddonPropertyService;
import com.atlassian.plugin.connect.api.web.condition.ConnectConditionContext;
import com.atlassian.sal.api.user.UserManager;
import com.atlassian.sal.api.user.UserProfile;

import com.google.common.collect.ImmutableMap;

import org.apache.commons.lang3.StringUtils;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.node.ArrayNode;
import org.codehaus.jackson.node.JsonNodeFactory;
import org.codehaus.jackson.node.ObjectNode;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static com.atlassian.plugin.connect.plugin.property.AddonEntityPropertyEqualToCondition.getValueForPath;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class AddonEntityPropertyEqualToConditionTest
{
    public static final JsonNode COMPLICATED_EXAMPLE, LEVEL_ONE, LEVEL_TWO;
    public static final String FAKE_ADD_ON_KEY = "fake.add-on.key";

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

    @Mock
    private AddonPropertyService addonPropertyService;

    @Mock
    private UserManager userManager;

    @InjectMocks
    private AddonEntityPropertyEqualToCondition sut;

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

    @Test(expected = PluginParseException.class)
    public void testInit__missing_property_key_param_throws_plugin_parse_exception() {
        sut.init(ImmutableMap.of(
            ConnectConditionContext.CONNECT_ADD_ON_KEY_KEY, FAKE_ADD_ON_KEY,
            "value", "true"
        ));
    }

    @Test(expected = PluginParseException.class)
    public void testInit__missing_value_param_throws_plugin_parse_exception() {
        sut.init(ImmutableMap.of(
            ConnectConditionContext.CONNECT_ADD_ON_KEY_KEY, FAKE_ADD_ON_KEY,
            "propertyKey", "some-key"
        ));
    }

    @Test(expected = PluginParseException.class)
    public void testInit__invalid_value_throws_plugin_parse_exception() {
        // The value JSON is invalid because it is missing an extra quote after element
        sut.init(ImmutableMap.of(
            ConnectConditionContext.CONNECT_ADD_ON_KEY_KEY, FAKE_ADD_ON_KEY,
            "propertyKey", "some-key",
            "value", "{\"some\", \"element}"
        ));
    }

    @Test
    public void testInit__provided_property_key_and_value_throws_no_exception() {
        sut.init(ImmutableMap.of(
            ConnectConditionContext.CONNECT_ADD_ON_KEY_KEY, FAKE_ADD_ON_KEY,
            "propertyKey", "some-key",
            "value", "true"
        ));
    }


    @Test
    public void testShouldDisplay__mising_actual_value_results_in_not_displayed() {
        when(addonPropertyService.getPropertyValue(any(UserProfile.class), anyString(), anyString(), anyString()))
            .thenReturn(new AddonPropertyService.GetServiceResult.Fail(AddonPropertyServiceImpl.OperationStatusImpl.PROPERTY_NOT_FOUND));

        assertFalse(sut.shouldDisplay(ImmutableMap.of()));
    }

    @Test
    public void testShouldDisplay__with_array_objects_matching_ordering_should_display() {
        final ObjectNode root = JsonNodeFactory.instance.objectNode();

        final ArrayNode array = root.putArray("array");
        array.add(true);
        array.add(false);

        final AddonProperty propertyResult = new AddonProperty("unimportant-key", root, 12345L);

        when(addonPropertyService.getPropertyValue(any(UserProfile.class), anyString(), anyString(), anyString()))
            .thenReturn(new AddonPropertyService.GetServiceResult.Success(propertyResult));

        sut.init(ImmutableMap.of(
            ConnectConditionContext.CONNECT_ADD_ON_KEY_KEY, FAKE_ADD_ON_KEY,
            "propertyKey", "some-key",
            "objectName", "array",
            "value", "[true, false]"
        ));

        assertTrue(sut.shouldDisplay(ImmutableMap.of()));
    }

    @Test
    public void testShouldDisplay__with_array_objects_mismatching_ordering_should_not_display() {
        final ObjectNode root = JsonNodeFactory.instance.objectNode();

        final ArrayNode array = root.putArray("array");
        array.add(true);
        array.add(false);

        final AddonProperty propertyResult = new AddonProperty("unimportant-key", root, 12345L);

        when(addonPropertyService.getPropertyValue(any(UserProfile.class), anyString(), anyString(), anyString()))
            .thenReturn(new AddonPropertyService.GetServiceResult.Success(propertyResult));

        sut.init(ImmutableMap.of(
            ConnectConditionContext.CONNECT_ADD_ON_KEY_KEY, FAKE_ADD_ON_KEY,
            "propertyKey", "some-key",
            "objectName", "array",
            "value", "[false, true]"
        ));

        assertFalse(sut.shouldDisplay(ImmutableMap.of()));
    }

    @Test
    public void testShouldDisplay__with_array_objects_extra_actual_nodes_should_not_display() {
        final ObjectNode root = JsonNodeFactory.instance.objectNode();

        final ArrayNode array = root.putArray("array");
        array.add(true);
        array.add(false);
        array.add(true);

        final AddonProperty propertyResult = new AddonProperty("unimportant-key", root, 12345L);

        when(addonPropertyService.getPropertyValue(any(UserProfile.class), anyString(), anyString(), anyString()))
            .thenReturn(new AddonPropertyService.GetServiceResult.Success(propertyResult));

        sut.init(ImmutableMap.of(
            ConnectConditionContext.CONNECT_ADD_ON_KEY_KEY, FAKE_ADD_ON_KEY,
            "propertyKey", "some-key",
            "objectName", "array",
            "value", "[true, false]"
        ));

        assertFalse(sut.shouldDisplay(ImmutableMap.of()));
    }

    @Test
    public void testShouldDisplay__with_array_objects_extra_expected_nodes_should_not_display() {
        final ObjectNode root = JsonNodeFactory.instance.objectNode();

        final ArrayNode array = root.putArray("array");
        array.add(true);
        array.add(false);

        final AddonProperty propertyResult = new AddonProperty("unimportant-key", root, 12345L);

        when(addonPropertyService.getPropertyValue(any(UserProfile.class), anyString(), anyString(), anyString()))
            .thenReturn(new AddonPropertyService.GetServiceResult.Success(propertyResult));

        sut.init(ImmutableMap.of(
            ConnectConditionContext.CONNECT_ADD_ON_KEY_KEY, FAKE_ADD_ON_KEY,
            "propertyKey", "some-key",
            "objectName", "array",
            "value", "[true, false, false]"
        ));

        assertFalse(sut.shouldDisplay(ImmutableMap.of()));
    }

    @Test
    public void testShouldDisplay__with_json_objects_ordered_and_identical_fields_should_display() {
        final ObjectNode actual = JsonNodeFactory.instance.objectNode();
        actual.put("one", "one");
        actual.put("two", "two");

        final ObjectNode expected = JsonNodeFactory.instance.objectNode();
        expected.put("one", "one");
        expected.put("two", "two");

        final AddonProperty propertyResult = new AddonProperty("unimportant-key", actual, 12345L);

        when(addonPropertyService.getPropertyValue(any(UserProfile.class), anyString(), anyString(), anyString()))
            .thenReturn(new AddonPropertyService.GetServiceResult.Success(propertyResult));

        sut.init(ImmutableMap.of(
            ConnectConditionContext.CONNECT_ADD_ON_KEY_KEY, FAKE_ADD_ON_KEY,
            "propertyKey", "some-key",
            "value", expected.toString()
        ));

        assertTrue(sut.shouldDisplay(ImmutableMap.of()));
    }

    @Test
    public void testShouldDisplay__with_json_objects_unordered_but_identical_fields_should_display() {
        final ObjectNode actual = JsonNodeFactory.instance.objectNode();
        actual.put("one", "one");
        actual.put("two", "two");

        final ObjectNode expected = JsonNodeFactory.instance.objectNode();
        expected.put("two", "two");
        expected.put("one", "one");

        final AddonProperty propertyResult = new AddonProperty("unimportant-key", actual, 12345L);

        when(addonPropertyService.getPropertyValue(any(UserProfile.class), anyString(), anyString(), anyString()))
            .thenReturn(new AddonPropertyService.GetServiceResult.Success(propertyResult));

        sut.init(ImmutableMap.of(
            ConnectConditionContext.CONNECT_ADD_ON_KEY_KEY, FAKE_ADD_ON_KEY,
            "propertyKey", "some-key",
            "value", expected.toString()
        ));

        assertTrue(sut.shouldDisplay(ImmutableMap.of()));
    }

    @Test
    public void testShouldDisplay__with_json_objects_extra_actual_fields_should_not_display() {
        final ObjectNode actual = JsonNodeFactory.instance.objectNode();
        actual.put("one", "one");
        actual.put("two", "two");

        final ObjectNode expected = JsonNodeFactory.instance.objectNode();
        expected.put("one", "one");

        final AddonProperty propertyResult = new AddonProperty("unimportant-key", actual, 12345L);

        when(addonPropertyService.getPropertyValue(any(UserProfile.class), anyString(), anyString(), anyString()))
            .thenReturn(new AddonPropertyService.GetServiceResult.Success(propertyResult));

        sut.init(ImmutableMap.of(
            ConnectConditionContext.CONNECT_ADD_ON_KEY_KEY, FAKE_ADD_ON_KEY,
            "propertyKey", "some-key",
            "value", expected.toString()
        ));

        assertFalse(sut.shouldDisplay(ImmutableMap.of()));
    }

    @Test
    public void testShouldDisplay__with_json_objects_extra_expected_fields_should_not_display() {
        final ObjectNode actual = JsonNodeFactory.instance.objectNode();
        actual.put("one", "one");

        final ObjectNode expected = JsonNodeFactory.instance.objectNode();
        expected.put("one", "one");
        expected.put("two", "two");

        final AddonProperty propertyResult = new AddonProperty("unimportant-key", actual, 12345L);

        when(addonPropertyService.getPropertyValue(any(UserProfile.class), anyString(), anyString(), anyString()))
            .thenReturn(new AddonPropertyService.GetServiceResult.Success(propertyResult));

        sut.init(ImmutableMap.of(
            ConnectConditionContext.CONNECT_ADD_ON_KEY_KEY, FAKE_ADD_ON_KEY,
            "propertyKey", "some-key",
            "value", expected.toString()
        ));

        assertFalse(sut.shouldDisplay(ImmutableMap.of()));
    }

    @Test
    public void testShouldDisplay__with_actual_value_and_objectName_shoud_be_displayed() {
        final AddonProperty propertyResult = new AddonProperty("unimportant-key", createThreeLayelJsonNode(), 12345L);

        when(addonPropertyService.getPropertyValue(any(UserProfile.class), anyString(), anyString(), anyString()))
            .thenReturn(new AddonPropertyService.GetServiceResult.Success(propertyResult));

        sut.init(ImmutableMap.of(
            ConnectConditionContext.CONNECT_ADD_ON_KEY_KEY, FAKE_ADD_ON_KEY,
            "propertyKey", "some-key",
            "objectName", "life.universe.everything",
            "value", "42"
        ));

        assertTrue(sut.shouldDisplay(ImmutableMap.of()));
    }

    @Test
    public void testShouldDisplay__with_actual_value_and_missing_objectName_shoud_not_be_displayed() {
        final AddonProperty propertyResult = new AddonProperty("unimportant-key", createThreeLayelJsonNode(), 12345L);

        when(addonPropertyService.getPropertyValue(any(UserProfile.class), anyString(), anyString(), anyString()))
            .thenReturn(new AddonPropertyService.GetServiceResult.Success(propertyResult));

        sut.init(ImmutableMap.of(
            ConnectConditionContext.CONNECT_ADD_ON_KEY_KEY, FAKE_ADD_ON_KEY,
            "propertyKey", "some-key",
            "objectName", "life.universe.allthethings",
            "value", "42"
        ));

        assertFalse(sut.shouldDisplay(ImmutableMap.of()));
    }

    private static JsonNode createThreeLayelJsonNode() {
        final ObjectNode root = JsonNodeFactory.instance.objectNode();
        root.put("not-important", "this data will not be read");

        final ObjectNode levelOne = root.putObject("life");
        levelOne.put("ignored", "data");

        final ObjectNode levelTwo = levelOne.putObject("universe");
        levelTwo.put("everything", 42);
        levelTwo.put("help", "it's dangerous to go alone. Here, take this towel");
        return root;
    }

    @Test
    public void testShouldDisplay__should_coerce_expected_boolean_into_string_comparison() {
        final AddonProperty propertyResult = new AddonProperty("unimportant-key", JsonNodeFactory.instance.textNode("true"), 12345L);
        when(addonPropertyService.getPropertyValue(any(UserProfile.class), anyString(), anyString(), anyString()))
            .thenReturn(new AddonPropertyService.GetServiceResult.Success(propertyResult));

        sut.init(ImmutableMap.of(
            ConnectConditionContext.CONNECT_ADD_ON_KEY_KEY, FAKE_ADD_ON_KEY,
            "propertyKey", "unimportant-key",
            "value", "true"
        ));

        assertTrue(sut.shouldDisplay(ImmutableMap.of()));
    }

    @Test
    public void testShouldDisplay__should_coerce_expected_number_into_string_comparison() {
        final AddonProperty propertyResult = new AddonProperty("unimportant-key", JsonNodeFactory.instance.textNode("10"), 12345L);
        when(addonPropertyService.getPropertyValue(any(UserProfile.class), anyString(), anyString(), anyString()))
            .thenReturn(new AddonPropertyService.GetServiceResult.Success(propertyResult));

        sut.init(ImmutableMap.of(
            ConnectConditionContext.CONNECT_ADD_ON_KEY_KEY, FAKE_ADD_ON_KEY,
            "propertyKey", "unimportant-key",
            "value", "10"
        ));

        assertTrue(sut.shouldDisplay(ImmutableMap.of()));
    }
}