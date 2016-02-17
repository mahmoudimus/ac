package it.jira.customfield;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import javax.ws.rs.core.Response.Status;

import com.atlassian.jira.functest.framework.Navigation;
import com.atlassian.jira.functest.framework.navigation.BulkChangeWizard;
import com.atlassian.jira.functest.framework.navigation.IssueNavigatorNavigation;
import com.atlassian.jira.pageobjects.dialogs.quickedit.CreateIssueDialog;
import com.atlassian.jira.pageobjects.pages.admin.customfields.CreateCustomFieldPage;
import com.atlassian.jira.pageobjects.pages.viewissue.ViewIssuePage;
import com.atlassian.jira.rest.api.customfield.CustomFieldDefinitionJsonBean;
import com.atlassian.jira.rest.api.issue.IssueCreateResponse;
import com.atlassian.jira.rest.api.issue.IssueFields;
import com.atlassian.jira.rest.api.issue.IssueUpdateRequest;
import com.atlassian.jira.testkit.beans.Screen;
import com.atlassian.jira.testkit.client.CustomFieldsControl;
import com.atlassian.jira.testkit.client.ScreensControl;
import com.atlassian.jira.testkit.client.restclient.FieldClient;
import com.atlassian.jira.testkit.client.restclient.Issue;
import com.atlassian.jira.testkit.client.restclient.IssueClient;
import com.atlassian.jira.testkit.client.restclient.Response;
import com.atlassian.jira.testkit.client.restclient.SearchRequest;
import com.atlassian.jira.testkit.client.restclient.SearchResult;
import com.atlassian.jira.testkit.client.restclient.WatchersClient;
import com.atlassian.pageobjects.elements.query.Poller;
import com.atlassian.plugin.connect.modules.beans.ConnectFieldModuleBean;
import com.atlassian.plugin.connect.modules.beans.ConnectFieldType;
import com.atlassian.plugin.connect.modules.beans.nested.I18nProperty;
import com.atlassian.plugin.connect.modules.beans.nested.ScopeName;
import com.atlassian.plugin.connect.test.common.servlet.ConnectRunner;
import com.atlassian.plugin.connect.test.common.util.AddonTestUtils;
import com.google.common.base.Objects;
import it.jira.JiraWebDriverTestBase;
import it.jira.util.JiraTestHelper;
import it.jira.util.MailTestHelper;
import org.apache.commons.lang.RandomStringUtils;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import static com.atlassian.plugin.connect.modules.beans.nested.VendorBean.newVendorBean;
import static it.jira.customfield.CustomFieldMatchers.customFieldResponse;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItem;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

public class TestConnectField extends JiraWebDriverTestBase
{
    private static final String FIELD_DESCRIPTION = "my description";
    private static String FIELD_NAME = "custom field title";

    private ConnectRunner addon;
    private String addonKey;
    private String fieldKey;

    private final CustomFieldsControl customFieldsControl;
    private final ScreensControl screensControl;
    private final IssueClient issueClient;
    private final Navigation navigation;

    public TestConnectField()
    {
        customFieldsControl = product.backdoor().customFields();
        screensControl = product.backdoor().screensControl();
        issueClient = new IssueClient(product.environmentData());
        navigation = JiraTestHelper.getNavigation();
    }

    @BeforeClass
    public static void setUpClass()
    {
        project = JiraTestHelper.addProject();
        product.backdoor().project().setNotificationScheme(Long.valueOf(project.getId()), 10000L);
        JiraTestHelper.addUserIfDoesNotExist("fred", "jira-software-users");
        product.quickLoginAsAdmin();
    }

    @Before
    public void setUp() throws Exception
    {
        fieldKey = "customfieldtype-key-" + org.apache.commons.lang3.RandomStringUtils.randomAlphanumeric(10); // random to have more or less independent tests

        addonKey = AddonTestUtils.randomAddonKey();

        addon = new ConnectRunner(product, addonKey)
                .setAuthenticationToNone()
                .setVendor(newVendorBean().withName("Atlassian").withUrl("http://www.atlassian.com").build())
                .addModules("jiraIssueFields",
                        buildIssueFieldModule(fieldKey, FIELD_NAME, FIELD_DESCRIPTION))
                .addScopes(ScopeName.READ)
                .start();
    }

    @After
    public void tearDown() throws Exception
    {
        if (addon != null)
        {
            addon.stopAndUninstall();
        }
    }

    @Test
    public void customFieldTypeIsNotVisibleOnCreateCustomFieldPage()
    {
        CreateCustomFieldPage page = product.quickLoginAsAdmin(CreateCustomFieldPage.class);

        assertThat(page.getAvailableCustomFields(), not(hasItem(customFieldTypeEntry(FIELD_NAME, FIELD_DESCRIPTION))));
    }

    @Test
    public void issueFieldIsVisibleOnCreateScreen()
    {
        String fieldId = getFieldId();
        addIssueFieldToScreens(project.getKey());

        IssueCreateResponse issue = createIssue();
        CreateIssueDialog createIssueDialog = openCreateIssueDialog(issue.key);

        List<String> visibleFields = createIssueDialog.getVisibleFields();
        assertThat(visibleFields, hasItem(equalTo(fieldId)));
    }

    @Test
    public void issueFieldIsEditableAndVisibleOnViewScreen()
    {
        String value = RandomStringUtils.randomAlphabetic(6);

        String fieldId = getFieldId();
        addIssueFieldToScreens(project.getKey());

        IssueCreateResponse issue = createIssue();
        CreateIssueDialog createIssueDialog = openCreateIssueDialog(issue.key);

        String issueKey = fillCreateIssueDialogCustomField(createIssueDialog, fieldId, value, issue);

        product.goToViewIssue(issueKey);
        String result = findCustomFieldWithIdOnPage(fieldId).getText();

        assertThat(result, equalTo(value));
    }

    @Test
    public void jqlSearchWorksForIssueField()
    {
        String value = RandomStringUtils.randomAlphabetic(6);

        String fieldId = getFieldId();
        addIssueFieldToScreens(project.getKey());
        String issueKey = createIssueWithCustomFieldViaREST(fieldId, value).key;

        String jql = "project = " + project.getKey() + " and \"" + FIELD_NAME + "\" ~ " + value;
        SearchResult searchResult = product.backdoor().search().loginAs("admin").getSearch(new SearchRequest().jql(jql));

        assertThat(searchResult.issues, hasItem(issue(issueKey)));
    }

    @Test
    public void tryingToCreateANewCustomFieldWithTheTypeOfTheIssueFieldFails()
    {
        FieldClient fieldClient = new FieldClient(product.environmentData());

        CustomFieldDefinitionJsonBean customFieldDefinitionJson = new CustomFieldDefinitionJsonBean(
                "name"
                , "desc",
                getCustomFieldTypeKey(),
                getCustomFieldSearcherKey(),
                null
        );

        Response response = fieldClient.createCustomFieldResponse(customFieldDefinitionJson);

        assertThat(response.statusCode, equalTo(Status.BAD_REQUEST.getStatusCode()));
    }

    @Test
    public void testEditableByREST() throws Exception
    {
        String fieldId = getFieldId();
        addIssueFieldToScreens(project.getKey());

        IssueCreateResponse issue = createIssueWithCustomFieldViaREST(fieldId, "edited via rest");

        assertThat(issueClient.get(issue.key).fields.get(fieldId), equalTo("edited via rest"));
    }

    @Test
    public void issueFieldCanBeEditedInBulkMode() throws Exception
    {
        List<IssueCreateResponse> createdIssues = IntStream.range(0, 5).mapToObj(i -> createTestIssue()).collect(Collectors.toList());

        String value = "bulk edited custom field value";

        navigation.issueNavigator().displayAllIssues();

        BulkChangeWizard wizard = navigation
                .issueNavigator()
                .bulkChange(IssueNavigatorNavigation.BulkChangeOption.CURRENT_PAGE)
                .selectAllIssues()
                .chooseOperation(BulkChangeWizard.BulkOperationsImpl.EDIT)
                .checkActionForField(getFieldId())
                .setFieldValue(getFieldId(), value)
                .finaliseFields()
                .complete()
                .waitForBulkChangeCompletion();

        assertEquals(wizard.getState(), BulkChangeWizard.WizardState.COMPLETE);

        createdIssues.forEach(issue ->
                assertThat(issueClient.get(issue.key).fields.get(getFieldId()), equalTo(value)));
    }

    @Test
    public void testUpdatingTheCustomFieldTriggersNotificationAndFieldValueIsPresentInEmails() throws Exception
    {
        MailTestHelper mailTestHelper = JiraTestHelper.setUpMailTest();

        // set up fred as a watcher
        addIssueFieldToScreens(project.getKey());
        IssueCreateResponse issue = createIssue();
        new WatchersClient(product.environmentData()).postResponse(issue.key, "fred");

        // update issue, fred should get an e-mail
        IssueUpdateRequest updateFieldRequest = new IssueUpdateRequest().fields(new IssueFields()
                .customField(numericFieldId(getFieldId()), "custom_field_new_value"));
        issueClient.update(issue.id, updateFieldRequest);

        // check if the e-mail was sent and that it contains our field
        assertThat(mailTestHelper.getSentMailContent(), containsString("custom_field_new_value"));
    }

    private IssueCreateResponse createTestIssue()
    {
        return product.backdoor().issues().createIssue(project.getKey(), "Issue with comments");
    }

    private IssueCreateResponse createIssueWithCustomFieldViaREST(final String fieldId, final String value)
    {
        IssueCreateResponse issue = createIssue();

        IssueUpdateRequest updateFieldRequest = new IssueUpdateRequest().fields(new IssueFields()
                .customField(numericFieldId(fieldId), value));

        issueClient.update(issue.id, updateFieldRequest);

        return issue;
    }

    /**
     * Currently depends on the name of the screen
     * @return screens that are tied to a project
     */
    private List<Screen> getScreensForProject(String projectKey)
    {
        List<Screen> allScreens = screensControl.getAllScreens();
        return allScreens.stream()
                .filter(screen -> screen.getName().startsWith(projectKey))
                .collect(Collectors.toList());
    }

    private void addIssueFieldToScreens(final String projectKey)
    {
        List<Screen> screensForProject = getScreensForProject(projectKey);
        screensForProject.stream()
                .forEach(screen -> screensControl.addFieldToScreen(screen.getName(), FIELD_NAME));
    }

    private String getFieldId()
    {
        return customFieldsControl.getCustomFields()
                .stream().filter((cf) ->
                        customFieldResponse(FIELD_NAME, FIELD_DESCRIPTION, getCustomFieldTypeKey(), getCustomFieldSearcherKey()).matches(cf))
                .map(cf -> cf.id)
                .findFirst().get();
    }

    private Long numericFieldId(String fullId)
    {
        return Long.parseLong(fullId.split("_")[1]);
    }

    private WebElement findCustomFieldWithIdOnPage(final String customFieldId)
    {
        return product.getTester().getDriver().findElement(By.id(customFieldId + "-val"));
    }

    private Matcher<Issue> issue(String key)
    {
        return new TypeSafeMatcher<Issue>()
        {
            @Override
            protected boolean matchesSafely(final Issue issue)
            {
                return issue.key.equals(key);
            }

            @Override
            public void describeTo(final Description description)
            {
                description.appendText("issue with key = ").appendValue(key);
            }
        };
    }

    private String getCustomFieldTypeKey()
    {
        return "com.atlassian.plugins.atlassian-connect-plugin:" + addonKey + "__" + fieldKey;
    }

    private String getCustomFieldSearcherKey()
    {
        return "com.atlassian.plugins.atlassian-connect-plugin:" + addonKey + "__" + fieldKey + "_searcher";
    }

    private IssueCreateResponse createIssue()
    {
        return product.backdoor().issues().createIssue(project.getKey(), "issue summary");
    }

    private String fillCreateIssueDialogCustomField(final CreateIssueDialog createIssueDialog, final String customFieldId, final String expectedValue, final IssueCreateResponse issue)
    {
        createIssueDialog.fill("summary", "summary");
        createIssueDialog.fill(customFieldId, expectedValue);

        String[] split = issue.key.split("-");
        String newIssueKey = split[0] + "-" + (Integer.parseInt(split[1]) + 1);
        createIssueDialog.submit(ViewIssuePage.class, issue.key);
        return newIssueKey;
    }

    private CreateIssueDialog openCreateIssueDialog(String issueKey)
    {
        ViewIssuePage viewIssuePage = product.goToViewIssue(issueKey);
        viewIssuePage.execKeyboardShortcut("c");

        CreateIssueDialog createIssueDialog = product.getPageBinder().bind(CreateIssueDialog.class, CreateIssueDialog.Type.ISSUE);
        Poller.waitUntilTrue("CreateIssueDialog was not opened.", createIssueDialog.isOpen());
        createIssueDialog.switchToFullMode();
        return createIssueDialog;
    }

    private Matcher<CreateCustomFieldPage.CustomFieldItem> customFieldTypeEntry(final String name, final String description)
    {
        return new TypeSafeMatcher<CreateCustomFieldPage.CustomFieldItem>()
        {
            @Override
            protected boolean matchesSafely(final CreateCustomFieldPage.CustomFieldItem customFieldItem)
            {
                return Objects.equal(customFieldItem.getName(), name)
                        && Objects.equal(customFieldItem.getDescription(), description);
            }

            @Override
            public void describeTo(final Description description)
            {
                description.appendValue(name);
            }
        };
    }

    private static ConnectFieldModuleBean buildIssueFieldModule(String key, String title, String description)
    {
        return ConnectFieldModuleBean.newBuilder()
                .withKey(key)
                .withName(new I18nProperty(title, null))
                .withDescription(new I18nProperty(description, null))
                .withBaseType(ConnectFieldType.TEXT)
                .build();
    }
}
