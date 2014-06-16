package it.com.atlassian.plugin.connect.workflow.jira;

import com.atlassian.jira.exception.CreateException;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.IssueFactory;
import com.atlassian.jira.issue.IssueManager;
import com.atlassian.jira.issue.MutableIssue;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.workflow.JiraWorkflow;
import com.atlassian.jira.workflow.WorkflowManager;
import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.connect.modules.beans.AuthenticationBean;
import com.atlassian.plugin.connect.modules.beans.AuthenticationType;
import com.atlassian.plugin.connect.modules.beans.ConnectAddonBean;
import com.atlassian.plugin.connect.modules.beans.LifecycleBean;
import com.atlassian.plugin.connect.modules.beans.WorkflowPostFunctionModuleBean;
import com.atlassian.plugin.connect.modules.beans.nested.I18nProperty;
import com.atlassian.plugin.connect.modules.beans.nested.UrlBean;
import com.atlassian.plugin.connect.testsupport.TestPluginInstaller;
import com.atlassian.plugin.connect.testsupport.filter.AddonTestFilterResults;
import com.atlassian.plugin.connect.testsupport.filter.ServletRequestSnaphot;
import com.atlassian.plugins.osgi.test.Application;
import com.atlassian.plugins.osgi.test.AtlassianPluginsTestRunner;
import it.com.atlassian.plugin.connect.TestAuthenticator;
import org.apache.http.HttpHeaders;
import org.json.JSONObject;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static com.atlassian.plugin.connect.modules.beans.ConnectAddonBean.newConnectAddonBean;
import static com.atlassian.plugin.connect.modules.beans.WorkflowPostFunctionModuleBean.newWorkflowPostFunctionBean;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

@Application("jira")
@RunWith(AtlassianPluginsTestRunner.class)
public class WorkflowPostFunctionTest
{
    private static final String PLUGIN_KEY = "connect-workflow-example";
    private static final String PLUGIN_NAME = "Workflow Post Function Test";
    private static final String MODULE_NAME = "My Post Function";
    private static final String MODULE_KEY = "wf-pf-test";
    private static final String TRIGGERED_URL = "/triggered";
    private static final String INSTALLED_URL = "/installed";

    private static final String ADMIN_USER = "admin";
    private static final String ISSUE_TYPE_BUG = "1";
    private static final long DEFAULT_PROJECT_ID = 10000L;
    private static final int RESOLVE_ACTION = 11;

    private final TestPluginInstaller testPluginInstaller;
    private final TestAuthenticator testAuthenticator;
    private final WorkflowManager workflowManager;
    private final IssueFactory issueFactory;
    private final IssueManager issueManager;
    private final JiraAuthenticationContext authenticationContext;
    private final AddonTestFilterResults testFilterResults;
    private final WorkflowImporter workflowImporter;

    private Plugin plugin;
    private JiraWorkflow workflow;

    public WorkflowPostFunctionTest(TestPluginInstaller testPluginInstaller,
                                    TestAuthenticator testAuthenticator,
                                    WorkflowManager workflowManager,
                                    IssueFactory issueFactory,
                                    IssueManager issueManager,
                                    JiraAuthenticationContext authenticationContext,
                                    AddonTestFilterResults testFilterResults,
                                    WorkflowImporter workflowImporter)
    {
        this.testPluginInstaller = testPluginInstaller;
        this.testAuthenticator = testAuthenticator;
        this.workflowManager = workflowManager;
        this.issueFactory = issueFactory;
        this.issueManager = issueManager;
        this.authenticationContext = authenticationContext;
        this.testFilterResults = testFilterResults;
        this.workflowImporter = workflowImporter;
    }

    @BeforeClass
    public void setup() throws Exception
    {
        testAuthenticator.authenticateUser(ADMIN_USER);

        WorkflowPostFunctionModuleBean bean = newWorkflowPostFunctionBean()
                .withName(new I18nProperty(MODULE_NAME, ""))
                .withDescription(new I18nProperty("My Description", "my.function.desc"))
                .withKey(MODULE_KEY)
                .withTriggered(new UrlBean(TRIGGERED_URL))
                .build();

        ConnectAddonBean addon = newConnectAddonBean()
                .withName(PLUGIN_NAME)
                .withKey(PLUGIN_KEY)
                .withBaseurl(testPluginInstaller.getInternalAddonBaseUrl(PLUGIN_KEY))
                .withAuthentication(AuthenticationBean.newAuthenticationBean().withType(AuthenticationType.JWT).build())
                .withLifecycle(LifecycleBean.newLifecycleBean().withInstalled(INSTALLED_URL).build())
                .withModules("jiraWorkflowPostFunctions", bean)
                .build();

        plugin = testPluginInstaller.installAddon(addon);
        workflow = workflowImporter.importWorkflow("postFunctionTestWorkflow", "/workflows/postFunctionTestWorkflow.xml");
    }

    @AfterClass
    public void cleanup() throws IOException
    {
        if (null != plugin)
        {
            testPluginInstaller.uninstallAddon(plugin);
        }
    }

    @Test
    public void requestsAreSent() throws Exception
    {
        ServletRequestSnaphot request = triggerWorkflowTransition();
        assertNotNull(request);
    }

    @Test
    public void requestsAreSigned() throws Exception
    {
        ServletRequestSnaphot request = triggerWorkflowTransition();
        String authorizationHeader = request.getHeaders().get(HttpHeaders.AUTHORIZATION.toLowerCase());
        assertNotNull(authorizationHeader);
        assertTrue(authorizationHeader.startsWith("JWT "));
    }

    @Test
    public void requestsContainTransition() throws Exception
    {
        ServletRequestSnaphot request = triggerWorkflowTransition();
        JSONObject payload = new JSONObject(request.getEntity());
        assertNotNull(payload.get("transition"));
    }

    @Test
    public void requestsContainIssue() throws Exception
    {
        ServletRequestSnaphot request = triggerWorkflowTransition();
        JSONObject payload = new JSONObject(request.getEntity());
        assertNotNull(payload.get("issue"));
    }

    @Test
    public void requestsContainAddonConfiguration() throws Exception
    {
        ServletRequestSnaphot request = triggerWorkflowTransition();
        JSONObject payload = new JSONObject(request.getEntity());
        assertNotNull(payload.get("configuration"));
    }

    private ServletRequestSnaphot triggerWorkflowTransition() throws CreateException
    {
        MutableIssue issue = issueManager.getIssueObject(createIssue("triggerWorkflowTransition").getId());
        workflowManager.migrateIssueToWorkflow(issue, workflow, issue.getStatusObject());
        workflowManager.doWorkflowAction(new WorkflowAction(authenticationContext.getUser(), issue, RESOLVE_ACTION));

        return getTriggeredRequest();
    }

    private Issue createIssue(String summary) throws CreateException
    {
        MutableIssue issueObject = issueFactory.getIssue();
        issueObject.setProjectId(DEFAULT_PROJECT_ID);
        issueObject.setIssueTypeId(ISSUE_TYPE_BUG);
        issueObject.setSummary(summary);
        issueObject.setReporterId(ADMIN_USER);

        Map params = new HashMap();
        params.put("issue", issueObject);

        return issueManager.createIssueObject(authenticationContext.getUser().getDirectoryUser(), params);
    }

    private ServletRequestSnaphot getTriggeredRequest()
    {
        return testFilterResults.getRequest(plugin.getKey(), TRIGGERED_URL);
    }
}
