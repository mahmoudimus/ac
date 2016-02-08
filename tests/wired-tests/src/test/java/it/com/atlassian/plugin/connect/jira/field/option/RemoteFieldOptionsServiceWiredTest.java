package it.com.atlassian.plugin.connect.jira.field.option;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

import com.atlassian.jira.bc.ServiceOutcome;
import com.atlassian.plugin.connect.api.util.JsonCommon;
import com.atlassian.plugin.connect.jira.field.FieldId;
import com.atlassian.plugin.connect.jira.field.option.RemoteFieldOption;
import com.atlassian.plugin.connect.jira.field.option.RemoteFieldOptionService;
import com.atlassian.plugins.osgi.test.Application;
import com.atlassian.plugins.osgi.test.AtlassianPluginsTestRunner;
import com.google.common.collect.ImmutableList;
import org.apache.commons.lang3.RandomStringUtils;
import org.codehaus.jackson.JsonNode;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static com.atlassian.plugin.connect.api.util.JsonCommon.parseStringToJson;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@Application ("jira")
@RunWith (AtlassianPluginsTestRunner.class)
public class RemoteFieldOptionsServiceWiredTest
{
    private final RemoteFieldOptionService remoteFieldOptionService;

    private FieldId fieldId;

    public RemoteFieldOptionsServiceWiredTest(final RemoteFieldOptionService remoteFieldOptionService)
    {
        this.remoteFieldOptionService = remoteFieldOptionService;
    }

    @Before
    public void setUp() throws Exception
    {
        fieldId = randomFieldId();
    }

    @Test
    public void optionCanBeCreated()
    {
        JsonNode jsonValue = parseStringToJson("42").get();
        RemoteFieldOption expectedResult = RemoteFieldOption.option(1, jsonValue);

        ServiceOutcome<RemoteFieldOption> result = remoteFieldOptionService.create(fieldId, jsonValue);
        assertTrue(result.isValid());
        assertEquals(expectedResult, result.get());

        List<RemoteFieldOption> allOptions = remoteFieldOptionService.get(fieldId).get();
        assertEquals(ImmutableList.of(expectedResult), allOptions);
    }

    @Test
    public void optionsGetConsecutiveIds()
    {
        createOptions(fieldId, "\"a\"", "\"b\"", "\"c\"", "\"d\"", "\"e\"");

        List<Integer> ids = remoteFieldOptionService.get(fieldId).get().stream().map(RemoteFieldOption::getId).collect(toList());
        assertEquals(ImmutableList.of(1, 2, 3, 4, 5), ids);
    }

    @Test
    public void everyOptionIsAlwaysAssignedAUniqueId()
    {
        createOptions(fieldId, "1", "2", "3", "4");
        remoteFieldOptionService.delete(fieldId, 3);
        remoteFieldOptionService.delete(fieldId, 2);
        createOption(fieldId, "\"a\"").getId();
        createOption(fieldId, "\"b\"").getId();

        Set<Integer> ids = remoteFieldOptionService.get(fieldId).get().stream().map(RemoteFieldOption::getId).collect(toSet());

        assertEquals(4, ids.size());
    }

    @Test
    public void gettingByIdWorks()
    {
        FieldId field1 = randomFieldId();
        FieldId field2 = randomFieldId();
        createOptions(field1, "\"a\"", "\"b\"", "\"c\"");
        createOptions(field2, "1", "2", "3");

        assertEquals(RemoteFieldOption.option(2, parseStringToJson("\"b\"").get()), remoteFieldOptionService.get(field1, 2).get());
        assertEquals(RemoteFieldOption.option(2, parseStringToJson("2").get()), remoteFieldOptionService.get(field2, 2).get());
    }

    @Test
    public void updatingWorks()
    {
        createOptions(fieldId, "\"a\"", "\"b\"", "\"c\"");
        RemoteFieldOption expectedValue = RemoteFieldOption.option(2, parseStringToJson("\"B\"").get());
        RemoteFieldOption result = remoteFieldOptionService.update(fieldId, expectedValue).get();
        assertEquals(expectedValue, result);
        assertEquals(expectedValue, remoteFieldOptionService.get(fieldId, 2).get());
    }

    private List<RemoteFieldOption> createOptions(final FieldId fieldId, String... values)
    {
        return Stream.of(values)
                .map(JsonCommon::parseStringToJson)
                .map(Optional::get).map(json -> remoteFieldOptionService.create(fieldId, json))
                .map(ServiceOutcome::get)
                .collect(toList());
    }

    private RemoteFieldOption createOption(FieldId fieldId, String value)
    {
        return remoteFieldOptionService.create(fieldId, parseStringToJson(value).get()).get();
    }

    private static FieldId randomFieldId()
    {
        return FieldId.fieldId(RandomStringUtils.randomAlphabetic(5), RandomStringUtils.randomAlphabetic(5));
    }
}
