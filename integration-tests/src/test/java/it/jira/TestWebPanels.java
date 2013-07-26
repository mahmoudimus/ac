
package it.jira;

import com.atlassian.plugin.remotable.test.jira.JiraProjectAdministrationPage;
import com.atlassian.plugin.remotable.test.pageobjects.RemoteWebPanel;
import com.atlassian.plugin.remotable.test.pageobjects.RemoteWebPanels;
import com.atlassian.plugin.remotable.test.pageobjects.jira.JiraViewIssuePage;
import com.atlassian.plugin.remotable.test.server.AtlassianConnectAddOnRunner;
import com.atlassian.plugin.remotable.test.server.module.IssuePanelPageModule;
import com.atlassian.plugin.remotable.test.server.module.ProjectConfigPanelModule;
import com.atlassian.plugin.remotable.test.server.module.RemoteWebPanelModule;
import hudson.plugins.jira.soap.RemoteIssue;
import it.MyContextAwareWebPanelServlet;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import java.rmi.RemoteException;

import static com.atlassian.plugin.remotable.test.Utils.createSignedRequestHandler;
import static com.atlassian.plugin.remotable.test.server.AtlassianConnectAddOnRunner.newMustacheServlet;
import static com.atlassian.plugin.remotable.test.server.AtlassianConnectAddOnRunner.newServlet;
import static it.TestConstants.ADMIN;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * Test of remote web panels in JIRA.
 */
public final class TestWebPanels extends JiraWebDriverTestBase
{
    // web panel locations
    private static final String ISSUE_PANEL_ID = "jira-remotePluginIssuePanelPage";
    private static final String ISSUE_REMOTE_LEFT_WEB_PANEL_ID = "jira-issue-left-web-panel";
    private static final String ISSUE_REMOTE_RIGHT_WEB_PANEL_ID = "jira-issue-right-web-panel";
    private static final String PROJECT_CONFIG_HEADER_WEB_PANEL = "jira-project-config-header-web-panel";
    private static final String PROJECT_CONFIG_PANEL_ID = "jira-remoteProjectConfigPanel";

    private static AtlassianConnectAddOnRunner remotePlugin;

    @BeforeClass
    public static void startConnectAddOn() throws Exception
    {
        remotePlugin = new AtlassianConnectAddOnRunner(product.getProductInstance().getBaseUrl())
                .addOAuth()
                .add(IssuePanelPageModule.key(ISSUE_PANEL_ID)
                        .name("AC Play Issue Page Panel")
                        .path("/ipp")
                        .resource(newMustacheServlet("iframe.mu")))
                .add(ProjectConfigPanelModule.key(PROJECT_CONFIG_PANEL_ID)
                        .name("AC Play Project Config Panel")
                        .path("/pcp")
                        .location("right")
                        .resource(newMustacheServlet("iframe.mu")))
                .add(RemoteWebPanelModule.key(ISSUE_REMOTE_LEFT_WEB_PANEL_ID)
                        .name("Issue Left Web Panel")
                        .location("atl.jira.view.issue.left.context")
                        .path("/ilwp")
                        .resource(newServlet(new MyContextAwareWebPanelServlet())))
                .add(RemoteWebPanelModule.key(ISSUE_REMOTE_RIGHT_WEB_PANEL_ID)
                        .name("Issue Right Web Panel")
                        .location("atl.jira.view.issue.right.context")
                        .path("/irwp")
                        .resource(newServlet(new MyContextAwareWebPanelServlet())))
                .add(RemoteWebPanelModule.key(PROJECT_CONFIG_HEADER_WEB_PANEL)
                        .name("Project Config Header Web Panel")
                        .location("atl.jira.proj.config.header")
                        .path("/pch")
                        .resource(newServlet(new MyContextAwareWebPanelServlet())))
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

    @Test
    public void testViewIssuePageWithEmbeddedPanelAnonymous() throws Exception
    {
        RemoteIssue issue = jiraOps.createIssue(project.getKey(), "Test issue for panel");
        JiraViewIssuePage viewIssuePage = product.visit(JiraViewIssuePage.class, issue.getKey());
        RemoteWebPanels webPanels = viewIssuePage.getWebPanels();
        assertNotNull("Web Panels should be found", webPanels);
        RemoteWebPanel panel = webPanels.getWebPanel(ISSUE_PANEL_ID);
        assertNotNull("Panel should be found", panel);
        assertEquals(issue.getId(), panel.getIssueId());
        assertEquals(project.getId(), panel.getProjectId());
    }

    @Test
    public void testViewProjectAdminPanel() throws Exception
    {
        loginAsAdmin();
        JiraProjectAdministrationPage projectAdministrationPage = product.visit(JiraProjectAdministrationPage.class, project.getKey());
        assertNotNull("Web panels of project administration page found", projectAdministrationPage.getWebPanels());
        RemoteWebPanel panel = projectAdministrationPage.getWebPanels().getWebPanel(PROJECT_CONFIG_PANEL_ID);
        assertNotNull("Remote panel should be found", panel);
        assertEquals(project.getId(), panel.getProjectId());
        assertEquals(ADMIN, panel.getUserId());
    }

    @Test
    public void testLeftWebPanelOnIssuePage() throws RemoteException
    {
        loginAsAdmin();
        RemoteIssue issue = jiraOps.createIssue(project.getKey(), "Test issue for left remotable-web-panel panel");
        RemoteWebPanels webPanels = product.visit(JiraViewIssuePage.class, issue.getKey()).getWebPanels();
        assertNotNull("Remote web panels should be found", webPanels);

        RemoteWebPanel panel = webPanels.getWebPanel(ISSUE_REMOTE_LEFT_WEB_PANEL_ID);
        assertNotNull("Remote panel should be found", panel);
        assertEquals(project.getId(), panel.getProjectId());
        assertEquals(issue.getId(), panel.getIssueId());
        assertEquals(ADMIN, panel.getUserId());
    }

    @Test
    public void testRightWebPanelOnIssuePage() throws RemoteException
    {
        loginAsAdmin();
        RemoteIssue issue = jiraOps.createIssue(project.getKey(), "Another test issue for right remotable-web-panel panel");
        RemoteWebPanels webPanels = product.visit(JiraViewIssuePage.class, issue.getKey()).getWebPanels();
        assertNotNull("Remote web panels should be found", webPanels);

        RemoteWebPanel panel = webPanels.getWebPanel(ISSUE_REMOTE_RIGHT_WEB_PANEL_ID);
        assertNotNull("Remote panel should be found", panel);
        assertEquals(project.getId(), panel.getProjectId());
        assertEquals(issue.getId(), panel.getIssueId());
        assertEquals(ADMIN, panel.getUserId());
    }

    @Test
    public void testWebPanelInProjectHeader()
    {
        loginAsAdmin();
        JiraProjectAdministrationPage projectAdministrationPage = product.visit(JiraProjectAdministrationPage.class, project.getKey());
        RemoteWebPanels webPanels = projectAdministrationPage.getWebPanels();
        assertNotNull("Remote web panels should be found", webPanels);

        RemoteWebPanel panel = webPanels.getWebPanel(PROJECT_CONFIG_HEADER_WEB_PANEL);
        assertNotNull("Remote panel should be found", panel);
        assertEquals(project.getId(), panel.getProjectId());
        assertEquals(ADMIN, panel.getUserId());
    }

    @Ignore("TODO: For some reason, there's an issue in the addLabelViaInlineEdit method where webdriver can't click on the submit button.")
    @Test
    public void testViewIssuePageWithEmbeddedPanelLoggedInWithEdit() throws Exception
    {
//        loginAsAdmin();
//        RemoteIssue issue = jiraOps.createIssue(project.getKey(), "Test issue for panel");
//        JiraViewIssuePage viewIssuePage = product.visit(JiraViewIssuePage.class, issue.getKey());
//
//        Assert.assertEquals("Success", viewIssuePage.getMessage());
//        viewIssuePage.addLabelViaInlineEdit("foo");
//        Assert.assertEquals("Success", viewIssuePage.getMessage());
    }
}

