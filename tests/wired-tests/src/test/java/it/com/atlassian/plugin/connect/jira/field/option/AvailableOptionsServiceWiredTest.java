package it.com.atlassian.plugin.connect.jira.field.option;

import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import com.atlassian.jira.bc.ServiceOutcome;
import com.atlassian.plugin.connect.jira.field.FieldId;
import com.atlassian.plugin.connect.jira.field.option.AvailableOption;
import com.atlassian.plugin.connect.jira.field.option.AvailableOptionsService;
import com.atlassian.plugin.connect.jira.field.option.JsonValue;
import com.atlassian.plugins.osgi.test.Application;
import com.atlassian.plugins.osgi.test.AtlassianPluginsTestRunner;
import com.google.common.collect.ImmutableList;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static java.util.stream.Collectors.toList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@Application ("jira")
@RunWith (AtlassianPluginsTestRunner.class)
public class AvailableOptionsServiceWiredTest
{
    private final AvailableOptionsService availableOptionsService;

    private FieldId fieldId;

    public AvailableOptionsServiceWiredTest(final AvailableOptionsService availableOptionsService)
    {
        this.availableOptionsService = availableOptionsService;
    }

    @Before
    public void setUp() throws Exception
    {
        fieldId = randomFieldId();
    }

    @Test
    public void optionCanBeCreated()
    {
        JsonValue jsonValue = JsonValue.parse("42").get();
        AvailableOption expectedResult = AvailableOption.option(1, jsonValue);

        ServiceOutcome<AvailableOption> result = availableOptionsService.create(fieldId, jsonValue);
        assertTrue(result.isValid());
        assertEquals(expectedResult, result.get());

        List<AvailableOption> allOptions = availableOptionsService.get(fieldId).get();
        assertEquals(ImmutableList.of(expectedResult), allOptions);
    }

    @Test
    public void optionsGetConsecutiveIds()
    {
        createOptions(fieldId, "a", "b", "c", "d", "e");

        List<Integer> ids = availableOptionsService.get(fieldId).get().stream().map(AvailableOption::getId).collect(toList());
        assertEquals(ImmutableList.of(1, 2, 3, 4, 5), ids);
    }

    @Test
    public void idGreaterThanEveryOtherIsAssignedToNewOptions()
    {
        createOptions(fieldId, "1", "2", "3", "4");
        availableOptionsService.delete(fieldId, 3);
        availableOptionsService.delete(fieldId, 2);

        assertEquals(Integer.valueOf(5), createOption(fieldId, "a").getId());
        assertEquals(Integer.valueOf(6), createOption(fieldId, "b").getId());
        availableOptionsService.delete(fieldId, 5);
        availableOptionsService.delete(fieldId, 6);
        assertEquals(Integer.valueOf(5), createOption(fieldId, "a").getId());
    }

    @Test
    public void gettingByIdWorks()
    {
        FieldId field1 = randomFieldId();
        FieldId field2 = randomFieldId();
        createOptions(field1, "a", "b", "c");
        createOptions(field2, "1", "2", "3");

        assertEquals(AvailableOption.option(2, JsonValue.parse("b").get()), availableOptionsService.get(field1, 2).get());
        assertEquals(AvailableOption.option(2, JsonValue.parse("2").get()), availableOptionsService.get(field2, 2).get());
    }

    @Test
    public void updatingWorks()
    {
        createOptions(fieldId, "a", "b", "c");
        AvailableOption expectedValue = AvailableOption.option(2, JsonValue.parse("B").get());
        AvailableOption result = availableOptionsService.update(fieldId, expectedValue).get();
        assertEquals(expectedValue, result);
        assertEquals(expectedValue, availableOptionsService.get(fieldId, 2).get());
    }

    private List<AvailableOption> createOptions(final FieldId fieldId, String... values)
    {
        return Stream.of(values)
                .map(JsonValue::parse)
                .map(Optional::get).map(json -> availableOptionsService.create(fieldId, json))
                .map(ServiceOutcome::get)
                .collect(toList());
    }

    private AvailableOption createOption(FieldId fieldId, String value)
    {
        return availableOptionsService.create(fieldId, JsonValue.parse(value).get()).get();
    }

    private static FieldId randomFieldId()
    {
        return FieldId.fieldId(RandomStringUtils.randomAlphabetic(5), RandomStringUtils.randomAlphabetic(5));
    }
}
