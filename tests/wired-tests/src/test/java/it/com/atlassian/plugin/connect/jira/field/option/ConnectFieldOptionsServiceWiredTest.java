package it.com.atlassian.plugin.connect.jira.field.option;

import com.atlassian.jira.bc.ServiceOutcome;
import com.atlassian.jira.bc.ServiceResult;
import com.atlassian.jira.exception.CreateException;
import com.atlassian.jira.exception.PermissionException;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.util.Page;
import com.atlassian.jira.util.PageRequest;
import com.atlassian.jira.util.PageRequests;
import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.connect.api.auth.AuthenticationData;
import com.atlassian.plugin.connect.jira.field.FieldId;
import com.atlassian.plugin.connect.jira.field.option.ConnectFieldOption;
import com.atlassian.plugin.connect.jira.field.option.ConnectFieldOptionScope;
import com.atlassian.plugin.connect.jira.field.option.ConnectFieldOptionService;
import com.atlassian.plugin.connect.jira.util.Json;
import com.atlassian.plugin.connect.modules.beans.AuthenticationBean;
import com.atlassian.plugin.connect.modules.beans.AuthenticationType;
import com.atlassian.plugin.connect.modules.beans.ConnectAddonBean;
import com.atlassian.plugin.connect.modules.beans.ConnectFieldModuleBean;
import com.atlassian.plugin.connect.modules.beans.ConnectFieldType;
import com.atlassian.plugin.connect.modules.beans.LifecycleBean;
import com.atlassian.plugin.connect.modules.beans.nested.I18nProperty;
import com.atlassian.plugin.connect.testsupport.TestPluginInstaller;
import com.atlassian.plugins.osgi.test.Application;
import com.atlassian.plugins.osgi.test.AtlassianPluginsTestRunner;
import com.atlassian.sal.api.user.UserManager;
import com.atlassian.sal.api.user.UserProfile;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import it.com.atlassian.plugin.connect.jira.util.JiraTestAuthenticator;
import it.com.atlassian.plugin.connect.jira.util.JiraTestUtil;
import org.apache.commons.lang3.RandomStringUtils;
import org.codehaus.jackson.JsonNode;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Stream;

import static com.atlassian.plugin.connect.jira.field.option.ConnectFieldOptionScope.GLOBAL;
import static com.atlassian.plugin.connect.jira.field.option.ConnectFieldOptionScope.project;
import static com.atlassian.plugin.connect.jira.util.Json.toJsonNode;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;

@Application("jira")
@RunWith(AtlassianPluginsTestRunner.class)
public class ConnectFieldOptionsServiceWiredTest {
    public static final PageRequest UNLIMITED_PAGE = PageRequests.request(null, null);
    private final ConnectFieldOptionService connectFieldOptionService;
    private final UserManager userManager;
    private final JiraTestUtil jiraTestUtil;
    private final TestPluginInstaller testPluginInstaller;
    private final JiraTestAuthenticator jiraTestAuthenticator;

    private List<Plugin> addons = Lists.newArrayList();

    private FieldId fieldId;
    private UserProfile admin;
    private AuthenticationData.User auth;
    private Project project;
    private UserProfile regularUser;

    public ConnectFieldOptionsServiceWiredTest(final ConnectFieldOptionService connectFieldOptionService, final UserManager userManager, final JiraTestUtil jiraTestUtil, final TestPluginInstaller testPluginInstaller, JiraTestAuthenticator jiraTestAuthenticator) {
        this.connectFieldOptionService = connectFieldOptionService;
        this.userManager = userManager;
        this.jiraTestUtil = jiraTestUtil;
        this.testPluginInstaller = testPluginInstaller;
        this.jiraTestAuthenticator = jiraTestAuthenticator;
    }

    @Before
    public void setUp() throws Exception {
        fieldId = randomFieldId();
        admin = userManager.getUserProfile(jiraTestUtil.getAdmin().getKey());
        regularUser = userManager.getUserProfile(jiraTestUtil.createUser().getKey());
        auth = AuthenticationData.byUser(admin);
        jiraTestAuthenticator.authenticateUser(admin.getUsername());
        project = jiraTestUtil.createProject();
    }

    @After
    public void tearDown() throws Exception {
        for (Plugin addon : addons) {
            testPluginInstaller.uninstallAddon(addon);
        }
    }

    @Test
    public void optionCanBeCreated() {
        JsonNode jsonValue = Json.parse("42").get();
        ConnectFieldOption expectedResult = ConnectFieldOption.of(1, jsonValue);

        ServiceOutcome<ConnectFieldOption> result = connectFieldOptionService.addOption(auth, fieldId, jsonValue, ConnectFieldOptionScope.GLOBAL);
        assertTrue(result.isValid());
        assertEquals(expectedResult, result.get());

        Page<ConnectFieldOption> allOptions = connectFieldOptionService.getOptions(auth, fieldId, UNLIMITED_PAGE).get();
        assertEquals(ImmutableList.of(expectedResult), allOptions.getValues());
    }

    @Test
    public void optionsGetConsecutiveIds() {
        createOptions(fieldId, "\"a\"", "\"b\"", "\"c\"", "\"d\"", "\"e\"");

        List<ConnectFieldOption> options = connectFieldOptionService.getOptions(auth, fieldId, UNLIMITED_PAGE).get().getValues();
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
    public void everyOptionIsAlwaysAssignedAUniqueId() {
        createOptions(fieldId, "1", "2", "3", "4");
        connectFieldOptionService.removeOption(auth, fieldId, 3);
        connectFieldOptionService.removeOption(auth, fieldId, 2);
        createOption(fieldId, "a").getId();
        createOption(fieldId, "b").getId();

        Set<Integer> ids = connectFieldOptionService.getOptions(auth, fieldId, UNLIMITED_PAGE).get().getValues().stream().map(ConnectFieldOption::getId).collect(toSet());

        assertEquals(4, ids.size());
    }

    @Test
    public void optionCanBeRetrievedById() {
        FieldId field1 = randomFieldId();
        FieldId field2 = randomFieldId();
        createOptions(field1, "\"a\"", "\"b\"", "\"c\"");
        createOptions(field2, "1", "2", "3");

        assertEquals(ConnectFieldOption.of(2, Json.parse("\"b\"").get()), connectFieldOptionService.getOption(auth, field1, 2).get());
        assertEquals(ConnectFieldOption.of(2, Json.parse("2").get()), connectFieldOptionService.getOption(auth, field2, 2).get());
    }

    @Test
    public void scopesWorkCorrectly() {
        connectFieldOptionService.addOption(auth, fieldId, toJsonNode("project 1.1"), project(1L));
        connectFieldOptionService.putOption(auth, fieldId, ConnectFieldOption.of(2, toJsonNode("project 1.2")).withScope(project(1L)));
        connectFieldOptionService.addOption(auth, fieldId, toJsonNode("project 2.1"), project(2L));
        connectFieldOptionService.putOption(auth, fieldId, ConnectFieldOption.of(4, toJsonNode("project 2.2")).withScope(project(2L)));
        connectFieldOptionService.addOption(auth, fieldId, toJsonNode("global 1"), ConnectFieldOptionScope.GLOBAL);
        connectFieldOptionService.putOption(auth, fieldId, ConnectFieldOption.of(6, toJsonNode("global 2")).withScope(GLOBAL));

        assertHasOptions(connectFieldOptionService.getOptions(auth, fieldId, UNLIMITED_PAGE),
                "project 1.1", "project 1.2", "project 2.1", "project 2.2", "global 1", "global 2");
        assertHasOptions(connectFieldOptionService.getOptions(auth, fieldId, UNLIMITED_PAGE, GLOBAL),
                "global 1", "global 2");
        assertHasOptions(connectFieldOptionService.getOptions(auth, fieldId, UNLIMITED_PAGE, project(1L)),
                "project 1.1", "project 1.2", "global 1", "global 2");
        assertHasOptions(connectFieldOptionService.getOptions(auth, fieldId, UNLIMITED_PAGE, project(2L)),
                "project 2.1", "project 2.2", "global 1", "global 2");
    }

    @Test
    public void scopeCanBeUpdated() {
        connectFieldOptionService.putOption(auth, fieldId, ConnectFieldOption.of(1, toJsonNode("option")).withScope(project(1L)));
        assertEquals(project(1L), connectFieldOptionService.getOption(auth, fieldId, 1).get().getScope());

        connectFieldOptionService.putOption(auth, fieldId, ConnectFieldOption.of(1, toJsonNode("option")).withScope(GLOBAL));
        assertEquals(GLOBAL, connectFieldOptionService.getOption(auth, fieldId, 1).get().getScope());

        connectFieldOptionService.putOption(auth, fieldId, ConnectFieldOption.of(1, toJsonNode("option")).withScope(project(2L)));
        assertEquals(project(2L), connectFieldOptionService.getOption(auth, fieldId, 1).get().getScope());
    }

    private void assertHasOptions(ServiceOutcome<Page<ConnectFieldOption>> result, String... expectedOptions) {
        List<String> options = result.get().getValues().stream()
                .map(ConnectFieldOption::getValue)
                .map(JsonNode::asText)
                .collect(toList());

        assertEquals(Arrays.asList(expectedOptions), options);
    }

    @Test
    public void optionCanBeUpdated() {
        createOptions(fieldId, "\"a\"", "\"b\"", "\"c\"");
        ConnectFieldOption expectedValue = ConnectFieldOption.of(2, Json.parse("\"B\"").get());
        ConnectFieldOption result = connectFieldOptionService.putOption(auth, fieldId, expectedValue).get();
        assertEquals(expectedValue, result);
        assertEquals(expectedValue, connectFieldOptionService.getOption(auth, fieldId, 2).get());
    }

    @Test
    public void optionWithSpecificIdCanBePut() {
        ConnectFieldOption option = ConnectFieldOption.of(42, Json.parse("42").get());
        ServiceOutcome<ConnectFieldOption> result = connectFieldOptionService.putOption(auth, fieldId, option);
        assertEquals(option, result.get());
        assertEquals(option, connectFieldOptionService.getOption(auth, fieldId, 42).get());
    }

    @Test
    public void replacementOptionMustExist() {
        ServiceResult serviceResult = connectFieldOptionService.replaceInAllIssues(auth, fieldId, 1, 2);
        assertEquals(ImmutableList.of("Option with id 2 not found"), serviceResult.getErrorCollection().getErrorMessages());
    }

    @Test
    public void replacingToTheSameValueIsNotAllowed() {
        ServiceResult serviceResult = connectFieldOptionService.replaceInAllIssues(auth, fieldId, 1, 1);
        assertEquals(ImmutableList.of("Replacement must be different than the current value"), serviceResult.getErrorCollection().getErrorMessages());
    }

    @Test
    public void testAuthorization() throws PermissionException, CreateException {

        jiraTestAuthenticator.unauthenticate();

        ImmutableList<AuthenticationData> notAuthorized = ImmutableList.of(
                AuthenticationData.byAddonKey("wrongAddonKey"),
                AuthenticationData.byRequest(mock(HttpServletRequest.class)),
                AuthenticationData.byUser(regularUser));

        ImmutableList<AuthenticationData> authorized = ImmutableList.of(
                AuthenticationData.byAddonKey(fieldId.getAddonKey()),
                AuthenticationData.byUser(admin));

        List<Function<AuthenticationData, ServiceResult>> methods = ImmutableList.of(
                data -> connectFieldOptionService.addOption(data, fieldId, Json.parse("23").get(), ConnectFieldOptionScope.GLOBAL),
                data -> connectFieldOptionService.putOption(data, fieldId, ConnectFieldOption.of(1, Json.parse("42").get())),
                data -> connectFieldOptionService.putOption(data, fieldId, ConnectFieldOption.of(2, Json.parse("42").get())),
                data -> connectFieldOptionService.replaceInAllIssues(data, fieldId, 1, 2),
                data -> connectFieldOptionService.getOption(data, fieldId, 1),
                data -> connectFieldOptionService.removeOption(data, fieldId, 1),
                data -> connectFieldOptionService.getOptions(data, fieldId, UNLIMITED_PAGE));

        notAuthorized.forEach(data -> methods.forEach(method -> {
            ServiceResult result = method.apply(data);
            assertFalse(result.isValid());
            assertEquals("Access denied (expected an authenticated add-on with key \"" + fieldId.getAddonKey() + "\" or a sysadmin)", result.getErrorCollection().getErrorMessages().iterator().next());
        }));

        authorized.forEach(data -> methods.forEach(method -> {
            ServiceResult result = method.apply(data);
            assertTrue(result.isValid());
        }));
    }

    @Test
    public void regularUsersAreAllowedToGetScopedOptions() {
        ConnectFieldOption globalOption = createOption(fieldId, "global", GLOBAL);
        ConnectFieldOption projectOption = createOption(fieldId, "project", project(this.project.getId()));

        assertEquals(ImmutableList.of(globalOption),
                connectFieldOptionService.getOptions(AuthenticationData.byUser(regularUser), fieldId, UNLIMITED_PAGE, GLOBAL).get().getValues());

        assertEquals(ImmutableList.of(globalOption, projectOption),
                connectFieldOptionService.getOptions(AuthenticationData.byUser(regularUser), fieldId, UNLIMITED_PAGE, project(project.getId())).get().getValues());
    }

    private List<ConnectFieldOption> createOptions(final FieldId fieldId, String... values) {
        return Stream.of(values)
                .map(Json::parse)
                .map(Optional::get)
                .map(json -> connectFieldOptionService.addOption(auth, fieldId, json, ConnectFieldOptionScope.GLOBAL))
                .map(ServiceOutcome::get)
                .collect(toList());
    }

    private ConnectFieldOption createOption(FieldId fieldId, String value) {
        return createOption(fieldId, value, GLOBAL);
    }

    private ConnectFieldOption createOption(FieldId fieldId, String value, ConnectFieldOptionScope scope) {
        return connectFieldOptionService.addOption(auth, fieldId, Json.toJsonNode(value), scope).get();
    }

    private FieldId randomFieldId() {
        FieldId fieldId = FieldId.of(RandomStringUtils.randomAlphabetic(5), RandomStringUtils.randomAlphabetic(5));
        try {
            // the field needs to be installed properly, actually
            addons.add(testPluginInstaller.installAddon(ConnectAddonBean.newConnectAddonBean()
                    .withKey(fieldId.getAddonKey())
                    .withBaseurl(testPluginInstaller.getInternalAddonBaseUrl(fieldId.getAddonKey()))
                    .withAuthentication(AuthenticationBean.newAuthenticationBean().withType(AuthenticationType.JWT).build())
                    .withLifecycle(LifecycleBean.newLifecycleBean().withInstalled("/installed").build())
                    .withModules("jiraIssueFields", ConnectFieldModuleBean.newBuilder()
                            .withKey(fieldId.getFieldKey())
                            .withName(new I18nProperty(fieldId.toString(), null))
                            .withBaseType(ConnectFieldType.TEXT)
                            .build())
                    .build()));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return fieldId;
    }
}
