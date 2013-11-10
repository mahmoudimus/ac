package it.capabilities.jira;

import com.atlassian.plugin.connect.plugin.capabilities.beans.nested.I18nProperty;
import com.atlassian.plugin.connect.test.pageobjects.RemoteWebItem;
import com.atlassian.plugin.connect.test.pageobjects.jira.JiraViewProjectPage;
import com.atlassian.plugin.connect.test.server.ConnectCapabilitiesRunner;
import com.google.common.base.Optional;
import it.jira.JiraWebDriverTestBase;
import it.servlet.ConnectAppServlets;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import java.rmi.RemoteException;

import static com.atlassian.plugin.connect.plugin.capabilities.beans.ConnectPageCapabilityBean.newPageBean;

/**
 * Test of general page in JIRA
 */
public class TestGeneralPage extends JiraWebDriverTestBase
{
    private static final String PLUGIN_KEY = "my-plugin";

    private static ConnectCapabilitiesRunner remotePlugin;
    private RemoteWebItem webItem;

    @BeforeClass
    public static void startConnectAddOn() throws Exception
    {
        remotePlugin = new ConnectCapabilitiesRunner(product.getProductInstance().getBaseUrl(), PLUGIN_KEY)
                .addCapability(
                        "generalPages",
                        newPageBean()
                                .withName(new I18nProperty("My Awesome Page", null))
                                .withUrl("/pg?issue_id=${issue.id}&project_id=${project.id}&project_key=${project.key}")
                                .withWeight(1234)
                                .build())
                .addRoute("/pg", ConnectAppServlets.apRequestServlet())
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
//        backdoor().project().addProject(PROJECT_KEY, PROJECT_KEY, "admin");
//
//        issueKey = jiraOps.createIssue(PROJECT_KEY, "Test issue for tab").getKey();
//        loginAsAdmin();
//        JiraViewProjectPage viewProjectPage = product.visit(JiraViewProjectPage.class, project.getKey());
//        webItem = viewProjectPage.findWebItem("my-awesome-page");
    }

    @After
    public void cleanUpTest()
    {
//        backdoor().project().deleteProject(PROJECT_KEY);
    }

    @Test
    public void canViewPage() throws RemoteException
    {
        loginAsAdmin();
        JiraViewProjectPage viewProjectPage = product.visit(JiraViewProjectPage.class, project.getKey());
        webItem = viewProjectPage.findWebItem("my-awesome-page", Optional.<String>absent());
//        loginAsAdmin();
//        JiraViewIssuePageWithRemotePluginIssueTab page = jira().visit(
//                JiraViewIssuePageWithRemotePluginIssueTab.class, "issue-tab-issue-tab-panel", issueKey, PLUGIN_KEY);
//        Assert.assertEquals("Success", page.getMessage());
    }
}
