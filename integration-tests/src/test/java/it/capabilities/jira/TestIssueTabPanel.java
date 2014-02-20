package it.capabilities.jira;

import com.atlassian.jira.functest.framework.FunctTestConstants;
import com.atlassian.jira.tests.TestBase;
import com.atlassian.plugin.connect.modules.beans.nested.I18nProperty;
import com.atlassian.plugin.connect.plugin.capabilities.provider.ConnectTabPanelModuleProvider;
import com.atlassian.plugin.connect.test.pageobjects.jira.JiraOps;
import com.atlassian.plugin.connect.test.pageobjects.jira.JiraViewIssuePage;
import com.atlassian.plugin.connect.test.pageobjects.jira.JiraViewIssuePageWithRemotePluginIssueTab;
import com.atlassian.plugin.connect.test.server.ConnectRunner;
import it.servlet.ConnectAppServlets;
import org.junit.*;
import org.junit.rules.TestRule;

import java.rmi.RemoteException;

import static com.atlassian.plugin.connect.modules.beans.ConnectTabPanelModuleBean.newTabPanelBean;
import static it.servlet.condition.ToggleableConditionServlet.toggleableConditionBean;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

/**
 * Test of remote issue tab panel in JIRA
 */
public class TestIssueTabPanel extends TestBase
{
    private static final String PLUGIN_KEY = "my-plugin";
    private static final String MODULE_KEY = "issue-tab-panel";
    private static JiraOps jiraOps = new JiraOps(jira().getProductInstance());
    private static ConnectRunner remotePlugin;
    private static final String PROJECT_KEY = FunctTestConstants.PROJECT_HOMOSAP_KEY;

    @Rule
    public TestRule resetToggleableCondition = remotePlugin.resetToggleableConditionRule();

    private String issueKey;

    @BeforeClass
    public static void startConnectAddOn() throws Exception
    {
        remotePlugin = new ConnectRunner(jira().getProductInstance().getBaseUrl(), PLUGIN_KEY)
                .setAuthenticationToNone()
                .addModule(ConnectTabPanelModuleProvider.ISSUE_TAB_PANELS, newTabPanelBean()
                        .withName(new I18nProperty("Issue Tab Panel", null))
                        .withKey(MODULE_KEY)
                        .withUrl("/ipp?issue_id={issue.id}&project_id={project.id}&project_key={project.key}")
                        .withConditions(toggleableConditionBean())
                        .withWeight(1234)
                        .build())
                .addRoute("/ipp", ConnectAppServlets.apRequestServlet())
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
        backdoor().project().addProject(PROJECT_KEY, PROJECT_KEY, "admin");

        issueKey = jiraOps.createIssue(PROJECT_KEY, "Test issue for tab").getKey();
    }

    @After
    public void cleanUpTest()
    {
        backdoor().project().deleteProject(PROJECT_KEY);
    }

    @Test
    public void testIssueTabPanel() throws RemoteException
    {
        jira().gotoLoginPage().loginAsSysadminAndGoToHome();
        JiraViewIssuePageWithRemotePluginIssueTab page = jira().visit(
                JiraViewIssuePageWithRemotePluginIssueTab.class, "issue-tab-panel", issueKey, PLUGIN_KEY);
        assertThat(page.getMessage(), is("Success"));
    }

    @Test
    public void tabIsNotAccessibleWithFalseCondition() throws RemoteException
    {
        jira().gotoLoginPage().loginAsSysadminAndGoToHome();

        // tab panel should be present
        JiraViewIssuePage page = jira().visit(JiraViewIssuePage.class, issueKey);
        assertThat(page.isTabPanelPresent(MODULE_KEY), is(true));

        remotePlugin.setToggleableConditionShouldDisplay(false);

        page = jira().visit(JiraViewIssuePage.class, issueKey);
        assertThat(page.isTabPanelPresent(MODULE_KEY), is(false));
    }
}
