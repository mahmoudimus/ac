package it.modules.jira;

import com.atlassian.jira.pageobjects.pages.viewissue.ViewIssuePage;
import com.atlassian.jira.pageobjects.util.Tracer;
import com.atlassian.plugin.connect.modules.beans.WebPanelModuleBean;
import com.atlassian.plugin.connect.modules.beans.nested.I18nProperty;
import com.atlassian.plugin.connect.test.pageobjects.jira.RemoteRefreshIssuePageWebPanel;
import com.atlassian.plugin.connect.test.server.ConnectRunner;
import hudson.plugins.jira.soap.RemoteIssue;
import it.jira.JiraWebDriverTestBase;
import it.servlet.ConnectAppServlets;
import it.util.TestUser;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import java.rmi.RemoteException;
import java.util.Collections;

/**
 * Integration tests for the JavaScript API method jira.refreshIssuePage().
 */
public class TestJiraRefreshIssuePage extends JiraWebDriverTestBase
{

    private static final String REFRESH_ISSUE_PAGE_WEB_PANEL_PATH = "/refresh-issue-page-web-panel";

    private static ConnectRunner addon;
    private static WebPanelModuleBean refreshIssuePageWebPanelModuleBean;

    private RemoteIssue issue;

    @BeforeClass
    public static void startAddon() throws Exception
    {
        refreshIssuePageWebPanelModuleBean = WebPanelModuleBean.newWebPanelBean()
                .withKey("refresh-issue-page-web-panel")
                .withName(new I18nProperty("Refresh Issue Page", null))
                .withLocation("atl.jira.view.issue.right.context")
                .withUrl(REFRESH_ISSUE_PAGE_WEB_PANEL_PATH)
                .build();
        addon = new ConnectRunner(product.getProductInstance().getBaseUrl(), "my-plugin")
                .setAuthenticationToNone()
                .addModules("webPanels", refreshIssuePageWebPanelModuleBean)
                .addRoute(REFRESH_ISSUE_PAGE_WEB_PANEL_PATH, ConnectAppServlets.refreshIssuePageButtonServlet())
                .start();
    }

    @AfterClass
    public static void stopAddon() throws Exception
    {
        if (addon != null)
        {
            addon.stopAndUninstall();
        }
    }

    @Before
    public void setUp() throws RemoteException
    {
        issue = jiraOps.createIssue(project.getKey(), "Test Issue");
    }

    @Test
    public void shouldRefreshIssuePage() throws RemoteException
    {
        ViewIssuePage viewIssuePage = loginAndVisit(TestUser.BARNEY, ViewIssuePage.class, issue.getKey());
        RemoteRefreshIssuePageWebPanel refreshIssuePageWebPanel = findRefreshIssuePageWebPanel();
        refreshIssuePageWebPanel.waitUntilRefreshIssuePageActionLoaded();

        jiraOps.updateIssue(issue.getKey(), Collections.singletonMap("summary", "foo"));

        Tracer tracer = refreshIssuePageWebPanel.refreshIssuePage();
        viewIssuePage.waitForAjaxRefresh(tracer);
    }

    private RemoteRefreshIssuePageWebPanel findRefreshIssuePageWebPanel()
    {
        String id = refreshIssuePageWebPanelModuleBean.getKey(addon.getAddon());
        return connectPageOperations.findWebPanel(id, RemoteRefreshIssuePageWebPanel.class);
    }
}
