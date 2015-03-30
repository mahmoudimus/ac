package it.jira.iframe;

import java.rmi.RemoteException;
import java.util.Map;

import com.atlassian.jira.functest.framework.FunctTestConstants;
import com.atlassian.plugin.connect.modules.beans.nested.I18nProperty;
import com.atlassian.plugin.connect.modules.beans.nested.ScopeName;
import com.atlassian.plugin.connect.plugin.capabilities.provider.ConnectTabPanelModuleProvider;
import com.atlassian.plugin.connect.test.AddonTestUtils;
import com.atlassian.plugin.connect.test.pageobjects.jira.JiraOps;
import com.atlassian.plugin.connect.test.pageobjects.jira.JiraViewIssuePage;
import com.atlassian.plugin.connect.test.pageobjects.jira.JiraViewIssuePageWithRemotePluginIssueTab;
import com.atlassian.plugin.connect.test.server.ConnectRunner;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestRule;

import hudson.plugins.jira.soap.RemoteIssue;
import it.jira.JiraWebDriverTestBase;
import it.servlet.ConnectAppServlets;
import it.servlet.condition.ParameterCapturingConditionServlet;

import static com.atlassian.plugin.connect.modules.beans.ConnectTabPanelModuleBean.newTabPanelBean;
import static com.atlassian.plugin.connect.modules.beans.nested.SingleConditionBean.newSingleConditionBean;
import static com.atlassian.plugin.connect.modules.util.ModuleKeyUtils.addonAndModuleKey;
import static it.servlet.condition.ParameterCapturingConditionServlet.PARAMETER_CAPTURE_URL;
import static it.servlet.condition.ToggleableConditionServlet.toggleableConditionBean;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasEntry;
import static org.hamcrest.core.Is.is;

/**
 * Test of remote issue tab panel in JIRA
 */
public class TestIssueTabPanel extends JiraWebDriverTestBase
{
    private static final String PLUGIN_KEY = AddonTestUtils.randomAddOnKey();
    private static final String MODULE_KEY = "issue-tab-panel";
    private static JiraOps jiraOps = new JiraOps(getProduct().getProductInstance());
    private static ConnectRunner remotePlugin;
    private static final String PROJECT_KEY = FunctTestConstants.PROJECT_HOMOSAP_KEY;

    @Rule
    public TestRule resetToggleableCondition = remotePlugin.resetToggleableConditionRule();

    private static final ParameterCapturingConditionServlet PARAMETER_CAPTURING_SERVLET = new ParameterCapturingConditionServlet();

    private long projectId;
    private RemoteIssue issue;

    @BeforeClass
    public static void startConnectAddOn() throws Exception
    {
        remotePlugin = new ConnectRunner(getProduct().getProductInstance().getBaseUrl(), PLUGIN_KEY)
                .setAuthenticationToNone()
                .addModule(ConnectTabPanelModuleProvider.ISSUE_TAB_PANELS, newTabPanelBean()
                        .withName(new I18nProperty("Issue Tab Panel", null))
                        .withKey(MODULE_KEY)
                        .withUrl("/ipp?issue_id={issue.id}&project_id={project.id}&project_key={project.key}")
                        .withConditions(
                            toggleableConditionBean(),
                            newSingleConditionBean().withCondition(PARAMETER_CAPTURE_URL +
                                    "?issue_id={issue.id}&issue_key={issue.key}&project_id={project.id}&project_key={project.key}").build()
                        )
                        .withWeight(1234)
                        .build())
                .addRoute("/ipp", ConnectAppServlets.apRequestServlet())
                .addRoute(PARAMETER_CAPTURE_URL, PARAMETER_CAPTURING_SERVLET)
                .addScope(ScopeName.READ)
                .start();
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
    public void setUpTest() throws Exception
    {
        projectId = getProduct().backdoor().project().addProject(PROJECT_KEY, PROJECT_KEY, "admin");

        issue = jiraOps.createIssue(PROJECT_KEY, "Test issue for tab");
    }

    @After
    public void cleanUpTest()
    {
        getProduct().backdoor().project().deleteProject(PROJECT_KEY);
    }

    @Test
    public void testIssueTabPanel() throws RemoteException
    {
        getProduct().gotoLoginPage().loginAsSysadminAndGoToHome();
        JiraViewIssuePageWithRemotePluginIssueTab page = getProduct().visit(
                JiraViewIssuePageWithRemotePluginIssueTab.class, "issue-tab-panel", issue.getKey(), PLUGIN_KEY);
        assertThat(page.getMessage(), is("Success"));

        Map<String,String> conditionRequestParams = PARAMETER_CAPTURING_SERVLET.getParamsFromLastRequest();
        assertThat(conditionRequestParams, hasEntry("issue_id", issue.getId()));
        assertThat(conditionRequestParams, hasEntry("issue_key", issue.getKey()));
        assertThat(conditionRequestParams, hasEntry("project_id", String.valueOf(projectId)));
        assertThat(conditionRequestParams, hasEntry("project_key", PROJECT_KEY));
    }

    @Test
    public void tabIsNotAccessibleWithFalseCondition() throws RemoteException
    {
        String completeKey = addonAndModuleKey(PLUGIN_KEY,MODULE_KEY);

        getProduct().gotoLoginPage().loginAsSysadminAndGoToHome();

        // tab panel should be present
        JiraViewIssuePage page = getProduct().visit(JiraViewIssuePage.class, issue.getKey());
        assertThat(page.isTabPanelPresent(completeKey), is(true));

        remotePlugin.setToggleableConditionShouldDisplay(false);

        page = getProduct().visit(JiraViewIssuePage.class, issue.getKey());
        assertThat(page.isTabPanelPresent(completeKey), is(false));
    }
}
