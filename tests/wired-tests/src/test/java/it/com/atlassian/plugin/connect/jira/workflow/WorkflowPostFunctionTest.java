package it.com.atlassian.plugin.connect.jira.workflow;

import com.atlassian.fugue.Option;
import com.atlassian.jira.bc.issue.comment.CommentService;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.exception.CreateException;
import com.atlassian.jira.issue.IssueManager;
import com.atlassian.jira.issue.MutableIssue;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.workflow.JiraWorkflow;
import com.atlassian.jira.workflow.WorkflowManager;
import com.atlassian.jira.workflow.WorkflowTransitionUtilImpl;
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
import com.atlassian.plugin.connect.testsupport.filter.ServletRequestSnapshot;
import com.atlassian.plugin.connect.testsupport.util.auth.TestAuthenticator;
import com.atlassian.plugin.util.WaitUntil;
import com.atlassian.plugins.osgi.test.Application;
import com.atlassian.plugins.osgi.test.AtlassianPluginsTestRunner;
import it.com.atlassian.plugin.connect.jira.util.JiraTestUtil;
import it.com.atlassian.plugin.connect.util.request.HeaderUtil;
import org.apache.http.HttpHeaders;
import org.json.JSONObject;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.IOException;

import static com.atlassian.plugin.connect.modules.beans.ConnectAddonBean.newConnectAddonBean;
import static com.atlassian.plugin.connect.modules.beans.WorkflowPostFunctionModuleBean.newWorkflowPostFunctionBean;
import static it.com.atlassian.plugin.connect.util.matcher.ParamMatchers.isVersionNumber;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

@Application("jira")
@RunWith(AtlassianPluginsTestRunner.class)
public class WorkflowPostFunctionTest {
    private static final String PLUGIN_KEY = "connect-workflow-example";
    private static final String PLUGIN_NAME = "Workflow Post Function Test";
    private static final String MODULE_NAME = "My Post Function";
    private static final String MODULE_KEY = "wf-pf-test";
    private static final String TRIGGERED_URL = "/triggered";
    private static final String INSTALLED_URL = "/installed";

    private static final String ADMIN_USER = "admin";
    private static final int RESOLVE_ACTION = 21;

    private final TestPluginInstaller testPluginInstaller;
    private final TestAuthenticator testAuthenticator;
    private final WorkflowManager workflowManager;
    private final IssueManager issueManager;
    private final JiraAuthenticationContext authenticationContext;
    private final AddonTestFilterResults testFilterResults;
    private final WorkflowImporter workflowImporter;
    private final JiraTestUtil jiraTestUtil;

    private Plugin plugin;
    private JiraWorkflow workflow;

    public WorkflowPostFunctionTest(TestPluginInstaller testPluginInstaller,
                                    TestAuthenticator testAuthenticator,
                                    WorkflowManager workflowManager,
                                    IssueManager issueManager,
                                    JiraAuthenticationContext authenticationContext,
                                    AddonTestFilterResults testFilterResults,
                                    WorkflowImporter workflowImporter,
                                    JiraTestUtil jiraTestUtil) {
        this.testPluginInstaller = testPluginInstaller;
        this.testAuthenticator = testAuthenticator;
        this.workflowManager = workflowManager;
        this.issueManager = issueManager;
        this.authenticationContext = authenticationContext;
        this.testFilterResults = testFilterResults;
        this.workflowImporter = workflowImporter;
        this.jiraTestUtil = jiraTestUtil;
    }

    @BeforeClass
    public void setup() throws Exception {
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

    @Before
    public void beforeEach() {
        testFilterResults.clearRequest(plugin.getKey(), TRIGGERED_URL);
    }

    @AfterClass
    public void cleanup() throws IOException {
        if (null != plugin) {
            testPluginInstaller.uninstallAddon(plugin);
        }
    }

    @Test
    public void requestsAreSent() throws Exception {
        ServletRequestSnapshot request = triggerWorkflowTransition();
        assertNotNull(request);
    }

    @Test
    public void requestsAreSigned() throws Exception {
        ServletRequestSnapshot request = triggerWorkflowTransition();
        String authorizationHeader = request.getHeaders().get(HttpHeaders.AUTHORIZATION.toLowerCase());
        assertNotNull(authorizationHeader);
        assertTrue(authorizationHeader.startsWith("JWT "));
    }

    @Test
    public void requestsContainTransition() throws Exception {
        ServletRequestSnapshot request = triggerWorkflowTransition();
        JSONObject payload = new JSONObject(request.getEntity());
        assertNotNull(payload.get("transition"));
    }

    @Test
    public void requestsContainIssue() throws Exception {
        ServletRequestSnapshot request = triggerWorkflowTransition();
        JSONObject payload = new JSONObject(request.getEntity());
        assertNotNull(payload.get("issue"));
    }

    @Test
    public void requestContainsVersionNumber() throws Exception {
        ServletRequestSnapshot request = triggerWorkflowTransition();
        Option<String> maybeVersion = HeaderUtil.getVersionHeader(request);
        assertThat(maybeVersion.get(), isVersionNumber());
    }

    @Test
    public void requestsContainAddonConfiguration() throws Exception {
        ServletRequestSnapshot request = triggerWorkflowTransition();
        JSONObject payload = new JSONObject(request.getEntity());
        assertNotNull(payload.get("configuration"));
    }

    private ServletRequestSnapshot triggerWorkflowTransition() throws CreateException, IOException {
        MutableIssue issue = issueManager.getIssueObject(jiraTestUtil.createIssue().getId());
        workflowManager.migrateIssueToWorkflow(issue, workflow, issue.getStatus());
        WorkflowTransitionUtilImpl workflowTransition = new WorkflowTransitionUtilImpl(
                authenticationContext,
                workflowManager,
                ComponentAccessor.getPermissionManager(),
                ComponentAccessor.getFieldScreenRendererFactory(),
                ComponentAccessor.getComponent(CommentService.class),
                ComponentAccessor.getI18nHelperFactory());
        workflowTransition.setAction(RESOLVE_ACTION);
        workflowTransition.setUserkey(authenticationContext.getUser().getKey());
        workflowTransition.setIssue(issue);

        workflowManager.doWorkflowAction(workflowTransition);

        return waitForWebhook(plugin.getKey(), TRIGGERED_URL);
    }

    private ServletRequestSnapshot waitForWebhook(final String addonKey, final String path) {
        final ServletRequestSnapshot[] request = {null};

        WaitUntil.invoke(new WaitUntil.WaitCondition() {
            @Override
            public boolean isFinished() {
                request[0] = testFilterResults.getRequest(addonKey, path);
                return null != request[0];
            }

            @Override
            public String getWaitMessage() {
                return "waiting for enable webhook post...";
            }
        }, 5);

        return request[0];
    }
}
