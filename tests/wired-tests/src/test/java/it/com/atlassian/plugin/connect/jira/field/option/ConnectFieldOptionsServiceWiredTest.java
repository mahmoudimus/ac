package it.com.atlassian.plugin.connect.jira.field.option;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

import com.atlassian.jira.bc.ServiceOutcome;
import com.atlassian.plugin.connect.jira.field.FieldId;
import com.atlassian.plugin.connect.jira.field.option.Json;
import com.atlassian.plugin.connect.jira.field.option.ConnectFieldOption;
import com.atlassian.plugin.connect.jira.field.option.ConnectFieldOptionService;
import com.atlassian.plugins.osgi.test.Application;
import com.atlassian.plugins.osgi.test.AtlassianPluginsTestRunner;
import com.google.common.collect.ImmutableList;
import org.apache.commons.lang3.RandomStringUtils;
import org.codehaus.jackson.JsonNode;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static com.atlassian.plugin.connect.jira.field.option.Json.parse;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@Application ("jira")
@RunWith (AtlassianPluginsTestRunner.class)
public class ConnectFieldOptionsServiceWiredTest
{
    private final ConnectFieldOptionService connectFieldOptionService;

    private FieldId fieldId;

    public ConnectFieldOptionsServiceWiredTest(final ConnectFieldOptionService connectFieldOptionService)
    {
        this.connectFieldOptionService = connectFieldOptionService;
    }

    @Before
    public void setUp() throws Exception
    {
        fieldId = randomFieldId();
    }

    @Test
    public void optionCanBeCreated()
    {
        JsonNode jsonValue = parse("42").get();
        ConnectFieldOption expectedResult = ConnectFieldOption.of(1, jsonValue);

        ServiceOutcome<ConnectFieldOption> result = connectFieldOptionService.addOption(fieldId, jsonValue);
        assertTrue(result.isValid());
        assertEquals(expectedResult, result.get());

        List<ConnectFieldOption> allOptions = connectFieldOptionService.getAllOptions(fieldId).get();
        assertEquals(ImmutableList.of(expectedResult), allOptions);
    }

    @Test
    public void optionsGetConsecutiveIds()
    {
        createOptions(fieldId, "\"a\"", "\"b\"", "\"c\"", "\"d\"", "\"e\"");

        List<ConnectFieldOption> options = connectFieldOptionService.getAllOptions(fieldId).get().stream().collect(toList());
        assertEquals(ImmutableList.of(
                        ConnectFieldOption.of(1, Json.parse("\"a\"").get()),
                        ConnectFieldOption.of(2, Json.parse("\"b\"").get()),
                        ConnectFieldOption.of(3, Json.parse("\"c\"").get()),
                        ConnectFieldOption.of(4, Json.parse("\"d\"").get()),
                        ConnectFieldOption.of(5, Json.parse("\"e\"").get())
                ),
                options);
    }

    @Test
    public void everyOptionIsAlwaysAssignedAUniqueId()
    {
        createOptions(fieldId, "1", "2", "3", "4");
        connectFieldOptionService.removeOption(fieldId, 3);
        connectFieldOptionService.removeOption(fieldId, 2);
        createOption(fieldId, "\"a\"").getId();
        createOption(fieldId, "\"b\"").getId();

        Set<Integer> ids = connectFieldOptionService.getAllOptions(fieldId).get().stream().map(ConnectFieldOption::getId).collect(toSet());

        assertEquals(4, ids.size());
    }

    @Test
    public void optionCanBeRetrievedById()
    {
        FieldId field1 = randomFieldId();
        FieldId field2 = randomFieldId();
        createOptions(field1, "\"a\"", "\"b\"", "\"c\"");
        createOptions(field2, "1", "2", "3");

        assertEquals(ConnectFieldOption.of(2, parse("\"b\"").get()), connectFieldOptionService.getOption(field1, 2).get());
        assertEquals(ConnectFieldOption.of(2, parse("2").get()), connectFieldOptionService.getOption(field2, 2).get());
    }

    @Test
    public void optionCanBeUpdated()
    {
        createOptions(fieldId, "\"a\"", "\"b\"", "\"c\"");
        ConnectFieldOption expectedValue = ConnectFieldOption.of(2, parse("\"B\"").get());
        ConnectFieldOption result = connectFieldOptionService.updateOption(fieldId, expectedValue).get();
        assertEquals(expectedValue, result);
        assertEquals(expectedValue, connectFieldOptionService.getOption(fieldId, 2).get());
    }

    private List<ConnectFieldOption> createOptions(final FieldId fieldId, String... values)
    {
        return Stream.of(values)
                .map(Json::parse)
                .map(Optional::get)
                .map(json -> connectFieldOptionService.addOption(fieldId, json))
                .map(ServiceOutcome::get)
                .collect(toList());
    }

    private ConnectFieldOption createOption(FieldId fieldId, String value)
    {
        return connectFieldOptionService.addOption(fieldId, parse(value).get()).get();
    }

    private static FieldId randomFieldId()
    {
        return FieldId.of(RandomStringUtils.randomAlphabetic(5), RandomStringUtils.randomAlphabetic(5));
    }
}
