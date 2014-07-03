package it.capabilities.jira;

import com.atlassian.jira.rest.api.issue.IssueCreateResponse;
import com.atlassian.jira.testkit.client.IssuesControl;
import com.atlassian.jira.testkit.client.ProjectControl;
import com.atlassian.jira.testkit.client.restclient.EntityPropertyClient;
import com.atlassian.jira.testkit.client.restclient.Issue;
import com.atlassian.jira.testkit.client.restclient.SearchClient;
import com.atlassian.jira.testkit.client.restclient.SearchRequest;
import com.atlassian.jira.testkit.client.restclient.SearchResult;
import com.atlassian.jira.testkit.client.util.TestKitLocalEnvironmentData;
import com.atlassian.jira.util.json.JSONException;
import com.atlassian.jira.util.json.JSONObject;
import com.atlassian.plugin.connect.modules.beans.nested.EntityPropertyIndexExtractionConfigurationBean;
import com.atlassian.plugin.connect.modules.beans.nested.EntityPropertyIndexKeyConfigurationBean;
import com.atlassian.plugin.connect.modules.beans.nested.EntityPropertyIndexType;
import com.atlassian.plugin.connect.modules.beans.nested.EntityPropertyType;
import com.atlassian.plugin.connect.modules.beans.nested.I18nProperty;
import com.atlassian.plugin.connect.test.AddonTestUtils;
import com.atlassian.plugin.connect.test.server.ConnectRunner;
import com.google.common.base.Function;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import org.hamcrest.Matchers;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.List;

import static com.atlassian.plugin.connect.modules.beans.EntityPropertyModuleBean.newEntityPropertyModuleBean;
import static org.junit.Assert.assertThat;

public class TestEntityProperty
{
    private static final TestKitLocalEnvironmentData localEnvironmentData = new TestKitLocalEnvironmentData();
    private static final String ATTACHMENT_PROPERTY_KEY = "attachment";
    private static final String PLUGIN_KEY = AddonTestUtils.randomAddOnKey();
    private static final String PROJECT_KEY = "EP";

    private static ConnectRunner remotePlugin;
    private static IssuesControl issueClient;
    private static EntityPropertyClient entityPropertyClient;
    private static ProjectControl projectControl;
    private static SearchClient searchClient;

    @BeforeClass
    public static void startConnectAddOn() throws Exception
    {

        List<EntityPropertyIndexExtractionConfigurationBean> extractions = Lists.newArrayList(
                new EntityPropertyIndexExtractionConfigurationBean("size", EntityPropertyIndexType.number),
                new EntityPropertyIndexExtractionConfigurationBean("extension", EntityPropertyIndexType.string)
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
        issueClient = new IssuesControl(localEnvironmentData);
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

    @Before
    public void setup()
    {
        projectControl.addProject("Entity Property", PROJECT_KEY, "admin");
    }

    @After
    public void tearDown()
    {
        projectControl.deleteProject(PROJECT_KEY);
    }

    @Test
    public void attachmentStringIssuePropertyIndexedAndSearchable() throws JSONException
    {
        IssueCreateResponse issue = issueClient.createIssue(PROJECT_KEY, "Issue with attachment data");

        // Issue property should be indexed during PUT operation
        JSONObject attachmentData = getAttachmentData();
        entityPropertyClient.put(issue.key, ATTACHMENT_PROPERTY_KEY, attachmentData);

        // Check the entity property was saved and is accessible for Connect add-on
        JSONObject entityProperty = new JSONObject(entityPropertyClient.get(issue.key, ATTACHMENT_PROPERTY_KEY).value);
        assertThat(entityProperty.getString("extension"), Matchers.is("jpg"));

        SearchResult byFileExtensionSearchResult =
                searchClient.getSearch(new SearchRequest().jql("issue.property[attachment].extension = \"jpg\""));
        assertHasIssues(byFileExtensionSearchResult, Lists.newArrayList(issue.key));
    }

    @Test
    public void attachmentIntegerIssuePropertyIndexedAndSearchable() throws JSONException
    {
        IssueCreateResponse issue = issueClient.createIssue(PROJECT_KEY, "Another issue with attachment data");

        // Issue property should be indexed during PUT operation
        JSONObject attachmentData = getAttachmentData();
        entityPropertyClient.put(issue.key, ATTACHMENT_PROPERTY_KEY, attachmentData);

        // Check the entity property was saved and is accessible for Connect add-on
        JSONObject entityProperty = new JSONObject(entityPropertyClient.get(issue.key, ATTACHMENT_PROPERTY_KEY).value);
        assertThat(entityProperty.getInt("size"), Matchers.is(10));

        // Check it is possible to search for the issues by the indexed property
        SearchResult byAttachmentSizeSearchResult =
                searchClient.getSearch(new SearchRequest().jql("issue.property[attachment].size > 5"));
        assertHasIssues(byAttachmentSizeSearchResult, Lists.newArrayList(issue.key));
    }

    private static JSONObject getAttachmentData()
    {
        return new JSONObject(ImmutableMap.<String, Object>of("size", 10, "extension", "jpg"));
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
