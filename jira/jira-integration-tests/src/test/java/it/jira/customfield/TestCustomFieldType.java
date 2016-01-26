package it.jira.customfield;

import com.atlassian.jira.functest.framework.RestoreBlankInstance;
import com.atlassian.jira.pageobjects.dialogs.quickedit.CreateIssueDialog;
import com.atlassian.jira.pageobjects.pages.admin.customfields.AssociateCustomFieldToScreenPage;
import com.atlassian.jira.pageobjects.pages.admin.customfields.ConfigureFieldDialog;
import com.atlassian.jira.pageobjects.pages.admin.customfields.CreateCustomFieldPage;
import com.atlassian.jira.pageobjects.pages.admin.customfields.TypeSelectionCustomFieldDialog;
import com.atlassian.jira.pageobjects.pages.admin.customfields.ViewCustomFields;
import com.atlassian.jira.pageobjects.pages.viewissue.ViewIssuePage;
import com.atlassian.jira.rest.api.issue.IssueCreateResponse;
import com.atlassian.jira.testkit.beans.CustomFieldResponse;
import com.atlassian.jira.testkit.client.restclient.Issue;
import com.atlassian.jira.testkit.client.restclient.SearchRequest;
import com.atlassian.jira.testkit.client.restclient.SearchResult;
import com.atlassian.pageobjects.elements.query.Poller;
import com.atlassian.plugin.connect.modules.beans.CustomFieldBaseType;
import com.atlassian.plugin.connect.modules.beans.CustomFieldBaseTypeConfiguration;
import com.atlassian.plugin.connect.modules.beans.CustomFieldTypeModuleBean;
import com.atlassian.plugin.connect.modules.beans.nested.I18nProperty;
import com.atlassian.plugin.connect.modules.beans.nested.ScopeName;
import com.atlassian.plugin.connect.test.common.servlet.ConnectRunner;
import com.atlassian.plugin.connect.test.common.util.AddonTestUtils;
import com.google.common.base.Objects;
import it.jira.JiraTestBase;
import it.jira.JiraWebDriverTestBase;
import org.apache.commons.lang.RandomStringUtils;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import java.util.List;

import static com.atlassian.plugin.connect.modules.beans.nested.VendorBean.newVendorBean;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItem;
import static org.junit.Assert.assertThat;

@RestoreBlankInstance
public class TestCustomFieldType extends JiraWebDriverTestBase
{

    private final String CFT_KEY = "customfieldtype-key";
    private final String CFT_DESCRIPTION = "my description";

    private ConnectRunner addon;
    private String addonKey;
    private String customFieldTypeName;

    @Before
    public void setUpClass() throws Exception
    {
        project = JiraTestBase.addProject();
        addonKey = AddonTestUtils.randomAddonKey();

        customFieldTypeName = "custom field title" + RandomStringUtils.randomAlphabetic(4);

        addon = new ConnectRunner(product, addonKey)
                .setAuthenticationToNone()
                .setVendor(newVendorBean().withName("Atlassian").withUrl("http://www.atlassian.com").build())
                .addModules("jiraCustomFieldTypes",
                        buildCustomFieldTypeModule(CFT_KEY, customFieldTypeName, CFT_DESCRIPTION))
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
    public void customFieldTypeIsVisibleOnCreateCustomFieldPage()
    {
        CreateCustomFieldPage page = product.quickLoginAsAdmin(CreateCustomFieldPage.class);

        assertThat(page.getAvailableCustomFields(), hasItem(customFieldTypeEntry(customFieldTypeName, CFT_DESCRIPTION)));
    }

    @Test
    public void canCreateACustomFieldFromARemoteCustomFieldType() {
        final ViewCustomFields viewCustomFields = product.quickLoginAsAdmin(ViewCustomFields.class);

        String name = randomName();
        String description = "description";

        addCustomFieldType(viewCustomFields, name, description, customFieldTypeName);

        List<CustomFieldResponse> customFields = product.backdoor().customFields().getCustomFields();
        assertThat(customFields, hasItem(
                customFieldResponse(name, description, getCustomFieldTypeKey(), getCustomFieldSearcherKey())));
    }

    @Test
    public void customFieldIsVisibleOnScreenAndCreateIssueDialogAndViewIssue()
    {
        String value = RandomStringUtils.randomAlphabetic(6);

        String customFieldId = createCustomFieldFromType(customFieldTypeName).id;
        String issueKey = createIssueWithCustomField(customFieldId, value);

        product.goToViewIssue(issueKey);
        String result = findCustomFieldWithIdOnPage(customFieldId).getText();

        assertThat(result, equalTo(value));
    }

    @Test
    public void jqlSearchWorksForTextCustomField()
    {
        CustomFieldResponse customFieldId = createCustomFieldFromType(customFieldTypeName);
        String value = RandomStringUtils.randomAlphabetic(6);

        String issueKey = createIssueWithCustomField(customFieldId.id, value);

        String jql = "project = " + project.getKey() + " and \"" + customFieldId.name + "\" ~ " + value;
        SearchResult searchResult = product.backdoor().search().loginAs("admin").getSearch(new SearchRequest().jql(jql));

        assertThat(searchResult.issues, hasItem(issue(issueKey)));
    }

    private String createIssueWithCustomField(final String customFieldId, final String value)
    {
        IssueCreateResponse issue = createIssue();
        CreateIssueDialog createIssueDialog = openCreateIssueDialog(issue.key);

        List<String> visibleFields = createIssueDialog.getVisibleFields();
        assertThat(visibleFields, hasItem(equalTo(customFieldId)));

        return fillCreateIssueDialogCustomField(createIssueDialog, customFieldId, value, issue);
    }

    private CustomFieldResponse createCustomFieldFromType(final String type)
    {
        ViewCustomFields viewCustomFields = product.quickLoginAsAdmin(ViewCustomFields.class);
        String name = randomName();
        String description = "description " + "testAddFromGlobalPage";

        addCustomFieldType(viewCustomFields, name, description, type);
        addFieldToAllScreens();

        return getCustomFieldWithName(name).get();
    }

    private String randomName()
    {
        return "rfc " + RandomStringUtils.randomAlphabetic(5);
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
        return "com.atlassian.plugins.atlassian-connect-plugin:" + addonKey + "__" + CFT_KEY;

    }

    private String getCustomFieldSearcherKey()
    {
        return "com.atlassian.plugins.atlassian-connect-plugin:" + addonKey + "__" + CFT_KEY + "_searcher";
    }

    private java.util.Optional<CustomFieldResponse> getCustomFieldWithName(String name)
    {
        return product.backdoor().customFields().getCustomFields().stream().filter((customField) -> name.equals(customField.name)).findFirst();
    }

    private IssueCreateResponse createIssue()
    {
        return product.backdoor().issues().createIssue(project.getKey(), "issue summary");
    }

        private void addFieldToAllScreens()
    {
        AssociateCustomFieldToScreenPage associateScreen = product.getPageBinder().bind(AssociateCustomFieldToScreenPage.class);

        associateScreen.selectRow((s) -> true);
        associateScreen.submit();
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

    private AssociateCustomFieldToScreenPage addCustomFieldType(final ViewCustomFields viewCustomFields, final String name, final String description, final String cftName)
    {
        TypeSelectionCustomFieldDialog typeSelectionDialog = viewCustomFields.addCustomField();

        showAllCustomFieldTypes(typeSelectionDialog);

        ConfigureFieldDialog configureFieldDialog = typeSelectionDialog
                .select(cftName).next()
                .name(name).description(description);

        AssociateCustomFieldToScreenPage associateCustomFieldToScreenPage = configureFieldDialog.nextAndThenAssociate();
        return associateCustomFieldToScreenPage;
    }

    private Matcher<CustomFieldResponse> customFieldResponse(final String name, final String description, final String type, final String searcher)
    {
        return new TypeSafeMatcher<CustomFieldResponse>()
        {
            @Override
            protected boolean matchesSafely(final CustomFieldResponse customFieldItem)
            {
                return Objects.equal(customFieldItem.name, name)
                        && Objects.equal(customFieldItem.description, description)
                        && Objects.equal(customFieldItem.type, type)
                        && Objects.equal(customFieldItem.searcher, searcher);
            }

            @Override
            public void describeTo(final Description desc)
            {
                desc.appendValue(name)
                    .appendValue(description)
                    .appendValue(type)
                    .appendValue(searcher);
            }
        };
    }

    private void showAllCustomFieldTypes(final TypeSelectionCustomFieldDialog typeSelectionCustomFieldDialog)
    {
        product.getTester().getDriver().getDriver().findElement(By.cssSelector("#customfields-select-type .dialog-page-menu .item-button:first-child")).click();
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

    private static CustomFieldTypeModuleBean buildCustomFieldTypeModule(String key, String title, String description)
    {
        return CustomFieldTypeModuleBean.newBuilder()
                .withKey(key)
                .withName(new I18nProperty(title, null))
                .withDescription(new I18nProperty(description, null))
                .withBaseTypeConfiguration(
                        CustomFieldBaseTypeConfiguration.newBuilder()
                                .withArchetype(CustomFieldBaseType.TEXT).build())
                .build();
    }
}
