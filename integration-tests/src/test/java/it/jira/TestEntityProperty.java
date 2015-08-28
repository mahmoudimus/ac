package it.jira;

import com.atlassian.jira.rest.api.issue.IssueCreateResponse;
import com.atlassian.jira.testkit.client.IssueTypeControl;
import com.atlassian.jira.testkit.client.IssuesControl;
import com.atlassian.jira.testkit.client.ProjectControl;
import com.atlassian.jira.testkit.client.restclient.*;
import com.atlassian.jira.testkit.client.util.TestKitLocalEnvironmentData;
import com.atlassian.jira.util.json.JSONException;
import com.atlassian.jira.util.json.JSONObject;
import com.atlassian.plugin.connect.modules.beans.nested.*;
import com.atlassian.plugin.connect.test.AddonTestUtils;
import com.atlassian.plugin.connect.test.server.ConnectRunner;
import com.atlassian.query.operator.Operator;
import com.google.common.base.Function;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import it.util.TestProject;
import org.hamcrest.Matchers;
import org.junit.*;

import java.util.List;

import static com.atlassian.plugin.connect.modules.beans.EntityPropertyModuleBean.newEntityPropertyModuleBean;
import static com.google.common.collect.Lists.newArrayList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

public class TestEntityProperty extends JiraTestBase
{
    private static final TestKitLocalEnvironmentData localEnvironmentData = new TestKitLocalEnvironmentData();
    private static final String ATTACHMENT_PROPERTY_KEY = "attachment";
    private static final String PLUGIN_KEY = AddonTestUtils.randomAddOnKey();
    private static final String JQL_ALIAS_ATTACHMENT_SIZE = "attachmentSize";
    private static final String JQL_ALIAS_ATTACHMENT_EXTENSION = "attachmentExtension";

    private static ConnectRunner remotePlugin;
    private static IssuesControl issueClient;
    private static EntityPropertyClient entityPropertyClient;
    private static ProjectControl projectControl;
    private static SearchClient searchClient;
    private TestProject testProject;

    @BeforeClass
    public static void startConnectAddOn() throws Exception
    {

        List<EntityPropertyIndexExtractionConfigurationBean> extractions = newArrayList(
                new EntityPropertyIndexExtractionConfigurationBean("size", EntityPropertyIndexType.number, JQL_ALIAS_ATTACHMENT_SIZE),
                new EntityPropertyIndexExtractionConfigurationBean("extension", EntityPropertyIndexType.string, JQL_ALIAS_ATTACHMENT_EXTENSION),
                new EntityPropertyIndexExtractionConfigurationBean("author", EntityPropertyIndexType.string) // this one doesn't have alias
        );

        EntityPropertyIndexKeyConfigurationBean keyConfigurationBean =
                new EntityPropertyIndexKeyConfigurationBean(extractions, ATTACHMENT_PROPERTY_KEY);


        remotePlugin = new ConnectRunner(localEnvironmentData.getBaseUrl().toString(), PLUGIN_KEY)
                .setAuthenticationToNone()
                .addModule(
                        "jiraEntityProperties",
                        newEntityPropertyModuleBean()
                                .withName(new I18nProperty("JIRA Attachment indexing", "jira.attachment.indexing"))
                                .withKey("jira-attachment-indexing")
                                .withKeyConfiguration(keyConfigurationBean)
                                .withEntityType(EntityPropertyType.issue)
                                .build()
                )
                .start();
        issueClient = new IssuesControl(localEnvironmentData, new IssueTypeControl(localEnvironmentData));
        entityPropertyClient = new EntityPropertyClient(localEnvironmentData, "issue");
        projectControl = new ProjectControl(localEnvironmentData);
        searchClient = new SearchClient(localEnvironmentData);
    }

    @AfterClass
    public static void stopConnectAddOn() throws Exception
    {
        if (remotePlugin != null)
        {
            remotePlugin.stopAndUninstall();
        }
    }

    /**
     * Create a new project for easily deleting all created issues in tear-down
     */
    @Before
    public void setup()
    {
        testProject = addProject();
    }

    @After
    public void tearDown()
    {
        projectControl.deleteProject(testProject.getKey());
    }

    @Test
    public void stringIssuePropertyIndexedAndSearchableByFullyQualifiedPropertyName() throws JSONException
    {
        setPropertyAndSearchForValue("issue.property[attachment].extension", Operator.EQUALS, "jpg");
    }

    @Test
    public void stringIssuePropertyIndexedAndSearchableByJqlAlias() throws JSONException
    {
        setPropertyAndSearchForValue(JQL_ALIAS_ATTACHMENT_EXTENSION, Operator.EQUALS, "jpg");
    }

    @Test
    public void issuePropertyIndexedAndSearchableByFullyQualifiedPropertyName() throws JSONException
    {
        setPropertyAndSearchForValue("issue.property[attachment].size", Operator.GREATER_THAN, "5");
    }

    @Test
    public void integerIssuePropertyIndexedAndSearchableByJqlAlias() throws JSONException
    {
        setPropertyAndSearchForValue(JQL_ALIAS_ATTACHMENT_SIZE, Operator.GREATER_THAN, "5");
    }

    @Test
    public void attachmentNonIndexedValueIndexed() throws JSONException
    {
        setPropertyAndSearchForValue("issue.property[attachment].author", Operator.EQUALS,  "\"luke skywalker\"");
    }

    @Test
    public void conflictingAliasFromTwoAddOns() throws Exception
    {
        final List<EntityPropertyIndexExtractionConfigurationBean> extractions = ImmutableList.of(
                // the same alias
                new EntityPropertyIndexExtractionConfigurationBean("extension", EntityPropertyIndexType.string, JQL_ALIAS_ATTACHMENT_EXTENSION)
        );
        final String secondPropertyName = "attachment2";
        final EntityPropertyIndexKeyConfigurationBean keyConfigurationBean = new EntityPropertyIndexKeyConfigurationBean(extractions, secondPropertyName);

        remotePlugin = new ConnectRunner(localEnvironmentData.getBaseUrl().toString(), "second-addon")
                .setAuthenticationToNone()
                .addModule(
                        "jiraEntityProperties",
                        newEntityPropertyModuleBean()
                                .withName(new I18nProperty("JIRA Conflicting Attachment indexing", "jira.conflicting.attachment.indexing"))
                                .withKey("jira-conflicting-attachment-indexing")
                                .withKeyConfiguration(keyConfigurationBean)
                                .withEntityType(EntityPropertyType.issue)
                                .build()
                )
                .start();

        IssueCreateResponse firstIssueWithProperty = issueClient.createIssue(testProject.getKey(), "First issue with attachment data");
        IssueCreateResponse secondIssueWithProperty = issueClient.createIssue(testProject.getKey(), "Second issue with attachment data");

        // set the issue property
        JSONObject attachmentData = getAttachmentData();
        entityPropertyClient.put(firstIssueWithProperty.key, ATTACHMENT_PROPERTY_KEY, attachmentData);
        entityPropertyClient.put(secondIssueWithProperty.key, secondPropertyName, attachmentData);

        // both found under general alias
        assertHasIssues(getSearchResult(JQL_ALIAS_ATTACHMENT_EXTENSION, Operator.EQUALS, "jpg"),
                newArrayList(firstIssueWithProperty.key, secondIssueWithProperty.key));

        // only the first issue found for `attachment` property
        assertHasIssues(getSearchResult("issue.property[attachment].extension", Operator.EQUALS, "jpg"), newArrayList(firstIssueWithProperty.key));

        // only the second issue found for `attachment2` property
        assertHasIssues(getSearchResult("issue.property[attachment2].extension", Operator.EQUALS, "jpg"), newArrayList(secondIssueWithProperty.key));

        remotePlugin.stopAndUninstall();
    }

    private void setPropertyAndSearchForValue(String jqlName, Operator operator, String searchValue) throws JSONException
    {
        IssueCreateResponse issue = issueClient.createIssue(testProject.getKey(), "Some issue with attachment data");

        // Issue property should be indexed during PUT operation
        JSONObject attachmentData = getAttachmentData();
        entityPropertyClient.put(issue.key, ATTACHMENT_PROPERTY_KEY, attachmentData);

        // Check the entity property was saved and is accessible for Connect add-on
        JSONObject entityProperty = new JSONObject(entityPropertyClient.get(issue.key, ATTACHMENT_PROPERTY_KEY).value);
        assertEquals(attachmentData, entityProperty);

        // Check it is possible to search for the issues by the indexed property
        SearchResult searchRequest = getSearchResult(jqlName, operator, searchValue);
        assertHasIssues(searchRequest, newArrayList(issue.key));
    }

    private SearchResult getSearchResult(final String jqlName, final Operator operator, final String searchValue)
    {
        return searchClient.getSearch(new SearchRequest().jql(String.format("%s %s %s", jqlName, operator.getDisplayString(), searchValue)));
    }

    private static JSONObject getAttachmentData()
    {
        return new JSONObject(ImmutableMap.<String, Object>of("size", 10, "extension", "jpg", "author", "luke skywalker"));
    }

    private static void assertHasIssues(SearchResult searchResult, List<String> issueKeys)
    {
        assertThat(searchResult.issues, Matchers.hasSize(issueKeys.size()));
        assertThat(Lists.transform(searchResult.issues, new Function<Issue, String>()
        {
            @Override
            public String apply(final Issue issue)
            {
                return issue.key;
            }
        }), Matchers.<String>hasItem(Matchers.isOneOf(issueKeys.toArray())));
    }
}
