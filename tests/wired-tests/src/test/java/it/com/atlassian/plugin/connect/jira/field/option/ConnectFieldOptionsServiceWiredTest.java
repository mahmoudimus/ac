package it.com.atlassian.plugin.connect.jira.field.option;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Stream;
import javax.servlet.http.HttpServletRequest;

import com.atlassian.jira.bc.ServiceOutcome;
import com.atlassian.jira.bc.ServiceResult;
import com.atlassian.jira.exception.CreateException;
import com.atlassian.jira.exception.PermissionException;
import com.atlassian.plugin.connect.jira.field.FieldId;
import com.atlassian.plugin.connect.jira.field.option.AuthenticationData;
import com.atlassian.plugin.connect.jira.field.option.ConnectFieldOption;
import com.atlassian.plugin.connect.jira.field.option.ConnectFieldOptionService;
import com.atlassian.plugin.connect.jira.field.option.Json;
import com.atlassian.plugins.osgi.test.Application;
import com.atlassian.plugins.osgi.test.AtlassianPluginsTestRunner;
import com.atlassian.sal.api.user.UserManager;
import com.atlassian.sal.api.user.UserProfile;
import com.google.common.collect.ImmutableList;
import it.com.atlassian.plugin.connect.jira.util.JiraTestUtil;
import org.apache.commons.lang3.RandomStringUtils;
import org.codehaus.jackson.JsonNode;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static com.atlassian.plugin.connect.jira.field.option.Json.parse;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;

@Application ("jira")
@RunWith (AtlassianPluginsTestRunner.class)
public class ConnectFieldOptionsServiceWiredTest
{
    private final ConnectFieldOptionService connectFieldOptionService;
    private final UserManager userManager;
    private final JiraTestUtil jiraTestUtil;

    private FieldId fieldId;
    private UserProfile admin;
    private AuthenticationData.User auth;

    public ConnectFieldOptionsServiceWiredTest(final ConnectFieldOptionService connectFieldOptionService, final UserManager userManager, final JiraTestUtil jiraTestUtil)
    {
        this.connectFieldOptionService = connectFieldOptionService;
        this.userManager = userManager;
        this.jiraTestUtil = jiraTestUtil;
    }

    @Before
    public void setUp() throws Exception
    {
        fieldId = randomFieldId();
        admin = userManager.getUserProfile(jiraTestUtil.getAdmin().getKey());
        auth = AuthenticationData.byUser(admin);
    }

    @Test
    public void optionCanBeCreated()
    {
        JsonNode jsonValue = parse("42").get();
        ConnectFieldOption expectedResult = ConnectFieldOption.of(1, jsonValue);

        ServiceOutcome<ConnectFieldOption> result = connectFieldOptionService.addOption(auth, fieldId, jsonValue);
        assertTrue(result.isValid());
        assertEquals(expectedResult, result.get());

        List<ConnectFieldOption> allOptions = connectFieldOptionService.getAllOptions(auth, fieldId).get();
        assertEquals(ImmutableList.of(expectedResult), allOptions);
    }

    @Test
    public void optionsGetConsecutiveIds()
    {
        createOptions(fieldId, "\"a\"", "\"b\"", "\"c\"", "\"d\"", "\"e\"");

        List<ConnectFieldOption> options = connectFieldOptionService.getAllOptions(auth, fieldId).get().stream().collect(toList());
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
        connectFieldOptionService.removeOption(auth, fieldId, 3);
        connectFieldOptionService.removeOption(auth, fieldId, 2);
        createOption(fieldId, "\"a\"").getId();
        createOption(fieldId, "\"b\"").getId();

        Set<Integer> ids = connectFieldOptionService.getAllOptions(auth, fieldId).get().stream().map(ConnectFieldOption::getId).collect(toSet());

        assertEquals(4, ids.size());
    }

    @Test
    public void optionCanBeRetrievedById()
    {
        FieldId field1 = randomFieldId();
        FieldId field2 = randomFieldId();
        createOptions(field1, "\"a\"", "\"b\"", "\"c\"");
        createOptions(field2, "1", "2", "3");

        assertEquals(ConnectFieldOption.of(2, parse("\"b\"").get()), connectFieldOptionService.getOption(auth, field1, 2).get());
        assertEquals(ConnectFieldOption.of(2, parse("2").get()), connectFieldOptionService.getOption(auth, field2, 2).get());
    }

    @Test
    public void optionCanBeUpdated()
    {
        createOptions(fieldId, "\"a\"", "\"b\"", "\"c\"");
        ConnectFieldOption expectedValue = ConnectFieldOption.of(2, parse("\"B\"").get());
        ConnectFieldOption result = connectFieldOptionService.putOption(auth, fieldId, expectedValue).get();
        assertEquals(expectedValue, result);
        assertEquals(expectedValue, connectFieldOptionService.getOption(auth, fieldId, 2).get());
    }

    @Test
    public void optionWithSpecificIdCanBePut()
    {
        ConnectFieldOption option = ConnectFieldOption.of(42, Json.parse("42").get());
        ServiceOutcome<ConnectFieldOption> result = connectFieldOptionService.putOption(auth, fieldId, option);
        assertEquals(option, result.get());
        assertEquals(option, connectFieldOptionService.getOption(auth, fieldId, 42).get());
    }

    @Test
    public void testAuthorization() throws PermissionException, CreateException
    {
        ImmutableList<AuthenticationData> notAuthorized = ImmutableList.of(
                AuthenticationData.byAddonKey("wrongAddonKey"),
                AuthenticationData.byRequest(mock(HttpServletRequest.class)),
                AuthenticationData.byUser(userManager.getUserProfile(jiraTestUtil.createUser().getKey())));

        ImmutableList<AuthenticationData> authorized = ImmutableList.of(
                AuthenticationData.byAddonKey(fieldId.getAddonKey()),
                AuthenticationData.byUser(admin));

        List<Function<AuthenticationData, ServiceResult>> methods = ImmutableList.of(
                data -> connectFieldOptionService.addOption(data, fieldId, Json.parse("23").get()),
                data -> connectFieldOptionService.putOption(data, fieldId, ConnectFieldOption.of(1, Json.parse("42").get())),
                data -> connectFieldOptionService.replaceInAllIssues(data, fieldId, 1, 2),
                data -> connectFieldOptionService.getOption(data, fieldId, 1),
                data -> connectFieldOptionService.removeOption(data, fieldId, 1),
                data -> connectFieldOptionService.getAllOptions(data, fieldId));

        notAuthorized.forEach(data -> methods.forEach(method -> {
            ServiceResult result = method.apply(data);
            assertFalse(result.isValid());
            assertEquals("Access denied (expected authenticated addon with key \"" + fieldId.getAddonKey() + "\" or a sysadmin)", result.getErrorCollection().getErrorMessages().iterator().next());
        }));

        authorized.forEach(data -> methods.forEach(method -> {
            ServiceResult result = method.apply(data);
            assertTrue(result.isValid());
        }));
    }

    private List<ConnectFieldOption> createOptions(final FieldId fieldId, String... values)
    {
        return Stream.of(values)
                .map(Json::parse)
                .map(Optional::get)
                .map(json -> connectFieldOptionService.addOption(auth, fieldId, json))
                .map(ServiceOutcome::get)
                .collect(toList());
    }

    private ConnectFieldOption createOption(FieldId fieldId, String value)
    {
        return connectFieldOptionService.addOption(auth, fieldId, parse(value).get()).get();
    }

    private static FieldId randomFieldId()
    {
        return FieldId.of(RandomStringUtils.randomAlphabetic(5), RandomStringUtils.randomAlphabetic(5));
    }
}
