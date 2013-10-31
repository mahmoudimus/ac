package it.capabilities.jira;

import java.rmi.RemoteException;

import com.atlassian.jira.functest.framework.FunctTestConstants;
import com.atlassian.jira.tests.TestBase;
import com.atlassian.plugin.connect.plugin.capabilities.beans.nested.I18nProperty;
import com.atlassian.plugin.connect.plugin.capabilities.provider.ConnectTabPanelModuleProvider;
import com.atlassian.plugin.connect.test.pageobjects.jira.JiraOps;
import com.atlassian.plugin.connect.test.pageobjects.jira.JiraViewIssuePageWithRemotePluginIssueTab;
import com.atlassian.plugin.connect.test.server.ConnectCapabilitiesRunner;

import org.junit.*;

import static com.atlassian.plugin.connect.plugin.capabilities.beans.ConnectTabPanelCapabilityBean.newTabPanelBean;
import static com.atlassian.plugin.connect.test.server.ConnectCapabilitiesRunner.newMustacheServlet;

/**
 * Test of remote issue tab panel in JIRA
 */
public class TestIssueTabPanel extends TestBase
{
    private static final String PLUGIN_KEY = "my-plugin";
    private static JiraOps jiraOps = new JiraOps(jira().getProductInstance());

    private static ConnectCapabilitiesRunner remotePlugin;
    private static final String PROJECT_KEY = FunctTestConstants.PROJECT_HOMOSAP_KEY;
    private String issueKey;

    @BeforeClass
    public static void startConnectAddOn() throws Exception
    {
        remotePlugin = new ConnectCapabilitiesRunner(jira().getProductInstance().getBaseUrl(), PLUGIN_KEY)
                .addCapability(ConnectTabPanelModuleProvider.ISSUE_TAB_PANELS, newTabPanelBean()
                        .withName(new I18nProperty("Issue Tab Panel", null))
                        .withUrl("/ipp?issue_id=${issue.id}&project_id=${project.id}&project_key=${project.key}")
                        .withWeight(1234)
                        .build())
                .addRoute("/ipp", newMustacheServlet("iframe.mu"))
                .start();
    }

    @AfterClass
    public static void stopConnectAddOn() throws Exception
    {
        if (remotePlugin != null)
        {
            remotePlugin.stop();
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
                JiraViewIssuePageWithRemotePluginIssueTab.class, "issue-tab-issue-tab-panel", issueKey, PLUGIN_KEY);
        Assert.assertEquals("Success", page.getMessage());
    }
}
