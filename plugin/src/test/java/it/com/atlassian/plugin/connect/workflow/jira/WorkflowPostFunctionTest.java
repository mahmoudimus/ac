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

@Application("jira")
@RunWith(AtlassianPluginsTestRunner.class)
public class WorkflowPostFunctionTest
{
    private static final String PLUGIN_KEY = "connect-workflow-example";
    private static final String PLUGIN_NAME = "Workflow Post Function Test";
    private static final String MODULE_NAME = "My Post Function";
    private static final String MODULE_KEY = "wf-pf-test";

    private static final String TRIGGERED = "/triggered";

    private final TestPluginInstaller testPluginInstaller;
    private final TestAuthenticator testAuthenticator;
    private final WorkflowManager workflowManager;
    private final WorkflowImporter workflowImporter;
    private final IssueFactory issueFactory;
    private final IssueManager issueManager;
    private final JiraAuthenticationContext authenticationContext;
    private final AddonTestFilterResults testFilterResults;

    private Plugin plugin;
    private JiraWorkflow workflow;

    public WorkflowPostFunctionTest(TestPluginInstaller testPluginInstaller,
                                    TestAuthenticator testAuthenticator,
                                    WorkflowManager workflowManager,
                                    WorkflowImporter workflowImporter,
                                    IssueFactory issueFactory,
                                    IssueManager issueManager,
                                    JiraAuthenticationContext authenticationContext,
                                    AddonTestFilterResults testFilterResults)
    {
        this.testPluginInstaller = testPluginInstaller;
        this.testAuthenticator = testAuthenticator;
        this.workflowManager = workflowManager;
        this.workflowImporter = workflowImporter;
        this.issueFactory = issueFactory;
        this.issueManager = issueManager;
        this.authenticationContext = authenticationContext;
        this.testFilterResults = testFilterResults;
    }

    @BeforeClass
    public void setup() throws Exception
    {
        testAuthenticator.authenticateUser("admin");

        WorkflowPostFunctionModuleBean bean = newWorkflowPostFunctionBean()
                .withName(new I18nProperty(MODULE_NAME, ""))
                .withKey(MODULE_KEY)
                .withDescription(new I18nProperty("My Description", "my.function.desc"))
                .withTriggered(new UrlBean(TRIGGERED))
                .build();

        ConnectAddonBean addon = newConnectAddonBean()
                .withName(PLUGIN_NAME)
                .withKey(PLUGIN_KEY)
                .withBaseurl(testPluginInstaller.getInternalAddonBaseUrl(PLUGIN_KEY))
                .withAuthentication(AuthenticationBean.newAuthenticationBean().withType(AuthenticationType.JWT).build())
                .withLifecycle(LifecycleBean.newLifecycleBean().withInstalled("/installed").build())
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
    }

    @Test
    public void requestsContainTransition() throws Exception
    {
    }

    @Test
    public void requestsContainIssue() throws Exception
    {
    }

    @Test
    public void requestsContainAddonConfiguration() throws Exception
    {
    }

    private ServletRequestSnaphot triggerWorkflowTransition() throws CreateException
    {
        MutableIssue issue = createIssue("test");
        workflowManager.migrateIssueToWorkflow(issue, workflow, issue.getStatusObject());
        workflowManager.doWorkflowAction(new WorkflowAction(authenticationContext.getUser(), issue, 11));

        return getTriggeredRequest();
    }

    private MutableIssue createIssue(String summary) throws CreateException
    {
        MutableIssue issueObject = issueFactory.getIssue();
        issueObject.setProjectId(10000L);
        issueObject.setIssueTypeId("1");
        issueObject.setSummary(summary);
        issueObject.setReporterId("admin");
        issueObject.setAssigneeId("admin");
        issueObject.setPriorityId("1");

        Map params = new HashMap();
        params.put("issue", issueObject);

        Issue issue = issueManager.createIssueObject(authenticationContext.getUser().getDirectoryUser(), params);
        return issueManager.getIssueObject(issue.getId());
    }

    private ServletRequestSnaphot getTriggeredRequest()
    {
        return testFilterResults.getRequest(plugin.getKey(), TRIGGERED);
    }
}
