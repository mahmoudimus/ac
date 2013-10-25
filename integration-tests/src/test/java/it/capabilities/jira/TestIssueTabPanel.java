package it.capabilities.jira;

import com.atlassian.jira.functest.framework.FunctTestConstants;
import com.atlassian.jira.rest.api.issue.IssueFields;
import com.atlassian.jira.rest.api.issue.IssueUpdateRequest;
import com.atlassian.jira.testkit.client.restclient.IssueClient;
import com.atlassian.jira.tests.TestBase;
import com.atlassian.plugin.connect.plugin.capabilities.beans.nested.I18nProperty;
import com.atlassian.plugin.connect.test.pageobjects.jira.JiraViewIssuePageWithRemotePluginIssueTab;
import com.atlassian.plugin.connect.test.server.ConnectCapabilitiesRunner;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import java.rmi.RemoteException;

import static com.atlassian.plugin.connect.plugin.capabilities.beans.ConnectIssueTabPanelCapabilityBean.newIssueTabPanelBean;
import static com.atlassian.plugin.connect.test.server.ConnectCapabilitiesRunner.newMustacheServlet;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

/**
 * Test of remote issue tab panel in JIRA
 */
public class TestIssueTabPanel extends TestBase
{
    private static ConnectCapabilitiesRunner remotePlugin;

    private static final String PROJECT_KEY = FunctTestConstants.PROJECT_HOMOSAP_KEY;
    private static final String JIRA_ISSUE_TAB_PANEL = "jira-issue-tab-panel";

    private String issueId;


    @BeforeClass
    public static void startConnectAddOn() throws Exception
    {
        remotePlugin = new ConnectCapabilitiesRunner(jira().getProductInstance().getBaseUrl(),"my-plugin")
                .addCapability(newIssueTabPanelBean()
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
        final IssueClient issueClient = new IssueClient(jira().environmentData());
        issueId = issueClient.create(new IssueUpdateRequest().fields(new IssueFields().summary("blah"))).id;
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
        final JiraViewIssuePageWithRemotePluginIssueTab issueTabPage = jira().goTo(JiraViewIssuePageWithRemotePluginIssueTab.class,
                PROJECT_KEY, issueId, "jira-issue-tab");

        assertThat(issueTabPage.getMessage(), equalTo("Sucess"));
    }
}
