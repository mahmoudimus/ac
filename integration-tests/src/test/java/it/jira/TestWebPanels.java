
package it.jira;

import com.atlassian.plugin.connect.api.xmldescriptor.XmlDescriptor;
import com.atlassian.plugin.connect.test.pageobjects.RemoteWebPanel;
import com.atlassian.plugin.connect.test.pageobjects.jira.JiraProjectAdministrationPage;
import com.atlassian.plugin.connect.test.pageobjects.jira.JiraViewIssuePage;
import com.atlassian.plugin.connect.test.pageobjects.jira.JiraViewProfilePage;
import com.atlassian.plugin.connect.test.server.AtlassianConnectAddOnRunner;
import com.atlassian.plugin.connect.test.server.module.IssuePanelPageModule;
import com.atlassian.plugin.connect.test.server.module.ProjectConfigPanelModule;
import com.atlassian.plugin.connect.test.server.module.RemoteWebPanelModule;
import hudson.plugins.jira.soap.RemoteIssue;
import it.servlet.ConnectAppServlets;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import java.rmi.RemoteException;

import static it.TestConstants.ADMIN_USERNAME;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * Test of remote web panels in JIRA.
 */
@XmlDescriptor
public final class TestWebPanels extends JiraWebDriverTestBase
{
    public static final String EXTRA_PREFIX = "remote-web-panel-";
    
    // web panel locations
    private static final String ISSUE_PANEL_ID = "jira-remotePluginIssuePanelPage";
    private static final String ISSUE_REMOTE_LEFT_WEB_PANEL_ID = "jira-issue-left-web-panel";
    private static final String ISSUE_REMOTE_LEFT_WEB_PANEL_ID_2 = "jira-issue-left-web-panel-2";
    private static final String ISSUE_REMOTE_RIGHT_WEB_PANEL_ID = "jira-issue-right-web-panel";
    private static final String USER_PROFILE_WEB_PANEL_ID = "user-profile-web-panel";
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
                        .path("/ipp?issue_id=${issue.id}&issue_key=${issue.key}&project_id=${project.id}&project_key=${project.key}")
                        .resource(ConnectAppServlets.apRequestServlet()))
                .add(ProjectConfigPanelModule.key(PROJECT_CONFIG_PANEL_ID)
                        .name("AC Play Project Config Panel")
                        .path("/pcp?issue_id=${issue.id}&project_id=${project.id}")
                        .location("right")
                        .resource(ConnectAppServlets.apRequestServlet()))
                .add(RemoteWebPanelModule.key(ISSUE_REMOTE_LEFT_WEB_PANEL_ID)
                        .name("Issue Left Web Panel")
                        .location("atl.jira.view.issue.left.context")
                        .path("/ilwp?issue_id=${issue.id}&project_id=${project.id}")
                        .resource(ConnectAppServlets.customMessageServlet("ilwp-OK")))
                .add(RemoteWebPanelModule.key(ISSUE_REMOTE_LEFT_WEB_PANEL_ID_2)
                        .name("Issue Left Web Panel 2")
                        .location("atl.jira.view.issue.left.context")
                        .path("/ilwp2?my-issue-id=${issue.id}&my-project-id=${project.id}")
                        .resource(ConnectAppServlets.customMessageServlet("ilwp2-OK")))
                .add(RemoteWebPanelModule.key(ISSUE_REMOTE_RIGHT_WEB_PANEL_ID)
                        .name("Issue Right Web Panel")
                        .location("atl.jira.view.issue.right.context")
                        .path("/irwp?issue_id=${issue.id}&project_id=${project.id}")
                        .resource(ConnectAppServlets.customMessageServlet("irwp-OK")))
                .add(RemoteWebPanelModule.key(PROJECT_CONFIG_HEADER_WEB_PANEL)
                        .name("Project Config Header Web Panel")
                        .location("atl.jira.proj.config.header")
                        .path("/pch?issue_id=${issue.id}&project_id=${project.id}")
                        .resource(ConnectAppServlets.customMessageServlet("pch-OK")))
                .add(RemoteWebPanelModule.key(USER_PROFILE_WEB_PANEL_ID)
                        .name("User Profile Web Panel")
                        .location("webpanels.user.profile.summary.custom")
                        .path("/up?profile_user_key=${profileUser.key}&profile_user_name=${profileUser.name}")
                        .resource(ConnectAppServlets.customMessageServlet("up-OK")))
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

    @Test
    public void testViewIssuePageWithEmbeddedPanelAnonymous() throws Exception
    {
        logout();
        RemoteIssue issue = jiraOps.createIssue(project.getKey(), "Test issue for panel");
        JiraViewIssuePage viewIssuePage = product.visit(JiraViewIssuePage.class, issue.getKey(), EXTRA_PREFIX);

        RemoteWebPanel panel = viewIssuePage.findWebPanel(ISSUE_PANEL_ID).waitUntilContentLoaded();

        assertEquals(issue.getId(), panel.getIssueId());
        assertEquals(issue.getKey(), panel.getFromQueryString("issue_key"));
        assertEquals(project.getId(), panel.getProjectId());
        assertEquals(project.getKey(), panel.getFromQueryString("project_key"));

        assertEquals("Success", panel.getApRequestMessage());
        assertEquals("200", panel.getApRequestStatusCode());
        assertEquals("401", panel.getApRequestUnauthorizedStatusCode());
    }

    @Test
    public void testViewIssuePageWithArbitraryDataInUrl() throws Exception
    {
        RemoteIssue issue = jiraOps.createIssue(project.getKey(), "Test issue for panel");
        JiraViewIssuePage viewIssuePage = product.visit(JiraViewIssuePage.class, issue.getKey(),EXTRA_PREFIX);
        RemoteWebPanel panel = viewIssuePage.findWebPanel(ISSUE_REMOTE_LEFT_WEB_PANEL_ID_2).waitUntilContentLoaded();

        assertEquals(issue.getId(), panel.getFromQueryString("my-issue-id"));
        assertEquals(project.getId(), panel.getFromQueryString("my-project-id"));

        assertEquals("ilwp2-OK", panel.getCustomMessage());
    }

    @Test
    public void testViewProjectAdminPanel() throws Exception
    {
        loginAsAdmin();
        JiraProjectAdministrationPage projectAdministrationPage = product.visit(JiraProjectAdministrationPage.class, project.getKey(), EXTRA_PREFIX);
        RemoteWebPanel panel = projectAdministrationPage.findWebPanel(PROJECT_CONFIG_PANEL_ID).waitUntilContentLoaded();

        assertEquals(project.getId(), panel.getProjectId());
        assertEquals(ADMIN_USERNAME, panel.getUserId());
		assertNotNull(panel.getUserKey());

        assertEquals("Success", panel.getApRequestMessage());
        assertEquals("200", panel.getApRequestStatusCode());
    }

    @Test
    public void testLeftWebPanelOnIssuePage() throws RemoteException
    {
        loginAsAdmin();
        RemoteIssue issue = jiraOps.createIssue(project.getKey(), "Test issue for left remotable-web-panel panel");
        RemoteWebPanel panel = product.visit(JiraViewIssuePage.class, issue.getKey(), EXTRA_PREFIX)
                .findWebPanel(ISSUE_REMOTE_LEFT_WEB_PANEL_ID).waitUntilContentLoaded();

        assertEquals(project.getId(), panel.getProjectId());
        assertEquals(issue.getId(), panel.getIssueId());
        assertEquals(ADMIN_USERNAME, panel.getUserId());
		assertNotNull(panel.getUserKey());

        assertEquals("ilwp-OK", panel.getCustomMessage());
    }

    @Test
    public void testRightWebPanelOnIssuePage() throws RemoteException
    {
        loginAsAdmin();
        RemoteIssue issue = jiraOps.createIssue(project.getKey(), "Another test issue for right remotable-web-panel panel");
        RemoteWebPanel panel = product.visit(JiraViewIssuePage.class, issue.getKey(), EXTRA_PREFIX)
                .findWebPanel(ISSUE_REMOTE_RIGHT_WEB_PANEL_ID).waitUntilContentLoaded();

        assertEquals(project.getId(), panel.getProjectId());
        assertEquals(issue.getId(), panel.getIssueId());
        assertEquals(ADMIN_USERNAME, panel.getUserId());
		assertNotNull(panel.getUserKey());

        assertEquals("irwp-OK", panel.getCustomMessage());
    }

    @Test
    public void testWebPanelInProjectHeader()
    {
        loginAsAdmin();
        JiraProjectAdministrationPage projectAdministrationPage = product.visit(JiraProjectAdministrationPage.class, project.getKey(), EXTRA_PREFIX);
        RemoteWebPanel panel = projectAdministrationPage
                .findWebPanel(PROJECT_CONFIG_HEADER_WEB_PANEL).waitUntilContentLoaded();

        assertEquals(project.getId(), panel.getProjectId());
        assertEquals(ADMIN_USERNAME, panel.getUserId());
		assertNotNull(panel.getUserKey());

        assertEquals("pch-OK", panel.getCustomMessage());
    }

    @Test
    public void testWebPanelInUserProfile()
    {
        final String userProfileName = "barney";

        loginAsAdmin();
        JiraViewProfilePage jiraViewProfilePage = product.visit(JiraViewProfilePage.class, userProfileName);
        RemoteWebPanel panel = jiraViewProfilePage.findWebPanelFromXMLAddOn(USER_PROFILE_WEB_PANEL_ID);

        assertEquals(userProfileName, panel.getFromQueryString("profile_user_key"));
        assertEquals(userProfileName, panel.getFromQueryString("profile_user_name"));
        assertEquals(ADMIN_USERNAME, panel.getUserId());
		assertNotNull(panel.getUserKey());

        assertEquals("up-OK", panel.getCustomMessage());
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

