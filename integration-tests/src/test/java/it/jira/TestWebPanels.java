
package it.jira;

import com.atlassian.plugin.remotable.test.RemoteWebPanel;
import com.atlassian.plugin.remotable.test.RemoteWebPanels;
import com.atlassian.plugin.remotable.test.jira.JiraProjectAdministrationPage;
import com.atlassian.plugin.remotable.test.jira.JiraViewIssuePage;
import com.atlassian.plugin.remotable.test.jira.JiraViewProfilePage;
import hudson.plugins.jira.soap.RemoteIssue;
import org.junit.Ignore;
import org.junit.Test;

import java.rmi.RemoteException;

import static it.TestConstants.ADMIN;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * Test of remote web panels in JIRA.
 */
public class TestWebPanels extends JiraWebDriverTestBase
{
    // web panel locations
    public static final String ISSUE_PANEL_ID = "jira-remotePluginIssuePanelPage";
    public static final String ISSUE_REMOTE_LEFT_WEB_PANEL_ID = "jira-issue-left-web-panel";
    public static final String ISSUE_REMOTE_RIGHT_WEB_PANEL_ID = "jira-issue-right-web-panel";
    public static final String USER_PROFILE_WEB_PANEL_ID = "user-profile-web-panel";
    public static final String PROJECT_CONFIG_HEADER_WEB_PANEL = "jira-project-config-header-web-panel";
    public static final String PROJECT_CONFIG_PANEL_ID = "jira-remoteProjectConfigPanel";

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

    @Test
    public void testWebPanelInUserProfile()
    {
        final String USER_PROFILE_NAME = "barney";

        loginAsAdmin();
        JiraViewProfilePage jiraViewProfilePage = product.visit(JiraViewProfilePage.class, USER_PROFILE_NAME);
        RemoteWebPanels webPanels = jiraViewProfilePage.getWebPanels();
        assertNotNull("Remote web panels should be found", webPanels);

        RemoteWebPanel panel = webPanels.getWebPanel(USER_PROFILE_WEB_PANEL_ID);
        assertNotNull("Remote panel should be found", panel);
        assertEquals(USER_PROFILE_NAME, panel.getProfileUserKey());
        assertEquals(USER_PROFILE_NAME, panel.getProfileUserName());
        assertEquals(ADMIN, panel.getUserId());
    }

    @Ignore ("TODO: For some reason, there's an issue in the addLabelViaInlineEdit method where webdriver can't click on the submit button.")
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

