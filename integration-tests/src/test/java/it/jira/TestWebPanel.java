
package it.jira;

import com.atlassian.pageobjects.page.HomePage;
import com.atlassian.plugin.connect.api.xmldescriptor.XmlDescriptor;
import com.atlassian.plugin.connect.modules.beans.nested.I18nProperty;
import com.atlassian.plugin.connect.test.pageobjects.RemoteWebPanel;
import com.atlassian.plugin.connect.test.pageobjects.jira.JiraProjectAdministrationPage;
import com.atlassian.plugin.connect.test.pageobjects.jira.JiraViewIssuePage;
import com.atlassian.plugin.connect.test.pageobjects.jira.JiraViewProfilePage;
import com.atlassian.plugin.connect.test.server.ConnectRunner;
import hudson.plugins.jira.soap.RemoteIssue;
import it.servlet.ConnectAppServlets;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import java.rmi.RemoteException;

import static com.atlassian.plugin.connect.modules.beans.WebItemModuleBean.newWebItemBean;
import static com.atlassian.plugin.connect.modules.beans.WebPanelModuleBean.newWebPanelBean;
import static it.TestConstants.ADMIN;
import static it.TestConstants.ADMIN_USERNAME;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * Test of remote web panels in JIRA.
 */
@XmlDescriptor
public final class TestWebPanel extends JiraWebDriverTestBase
{
    // web panel keys
    private static final String ISSUE_PANEL_LEFT_KEY = "jira-issue-left-web-panel";
    private static final String ISSUE_PANEL_LEFT2_KEY = "jira-issue-left-web-panel-2";
    private static final String ISSUE_PANEL_RIGHT_KEY = "jira-issue-right-web-panel";
    private static final String USER_PROFILE_KEY = "user-profile-web-panel";
    private static final String PROJECT_CONFIG_HEADER_KEY = "jira-project-config-header-web-panel";
    private static final String PROJECT_CONFIG_PANEL_KEY = "web-panel-project-config";

    private static ConnectRunner runner;

    @BeforeClass
    public static void startConnectAddOn() throws Exception
    {
        runner = new ConnectRunner(product)
                .setAuthenticationToNone()
                .addModules(
                        "webPanels",
                        newWebPanelBean()
                                .withKey(PROJECT_CONFIG_PANEL_KEY)
                                .withName(new I18nProperty("Project Config Panel", "pcp"))
                                .withLocation("webpanels.admin.summary.right-panels")
                                .withUrl("/pcp?issue_id=${issue.id}&project_id=${project.id}")
                                .build(),
                        newWebPanelBean()
                                .withKey(ISSUE_PANEL_LEFT_KEY)
                                .withName(new I18nProperty("Issue Left Web Panel", "ilwp"))
                                .withLocation("atl.jira.view.issue.left.context")
                                .withUrl("/ilwp?issue_id=${issue.id}&project_id=${project.id}")
                                .build(),
                        newWebPanelBean()
                                .withKey(ISSUE_PANEL_LEFT2_KEY)
                                .withName(new I18nProperty("Issue Left Web Panel 2", "ilwp2"))
                                .withLocation("atl.jira.view.issue.left.context")
                                .withUrl("/ilwp2?my-issue-id=${issue.id}&my-project-id=${project.id}")
                                .build(),
                        newWebPanelBean()
                                .withKey(ISSUE_PANEL_RIGHT_KEY)
                                .withName(new I18nProperty("Issue Right Web Panel", "irwp"))
                                .withLocation("atl.jira.view.issue.right.context")
                                .withUrl("/irwp?issue_id=${issue.id}&project_id=${project.id}")
                                .build(),
                        newWebPanelBean()
                                .withKey(PROJECT_CONFIG_HEADER_KEY)
                                .withName(new I18nProperty("Project Config Header Web Panel", "pch"))
                                .withLocation("atl.jira.proj.config.header")
                                .withUrl("/pch?issue_id=${issue.id}&project_id=${project.id}")
                                .build(),
                        newWebPanelBean()
                                .withKey(USER_PROFILE_KEY)
                                .withName(new I18nProperty("User Profile Web Panel", "up"))
                                .withLocation("webpanels.user.profile.summary.custom")
                                .withUrl("/up?profile_user_key=${profileUser.key}&profile_user_name=${profileUser.name}")
                                .build()
                )
                .addRoute("/pcp", ConnectAppServlets.customMessageServlet("pcp-OK"))
                .addRoute("/ilwp", ConnectAppServlets.customMessageServlet("ilwp-OK"))
                .addRoute("/ilwp2", ConnectAppServlets.customMessageServlet("ilwp2-OK"))
                .addRoute("/irwp", ConnectAppServlets.customMessageServlet("irwp-OK"))
                .addRoute("/pch", ConnectAppServlets.customMessageServlet("pch-OK"))
                .addRoute("/up", ConnectAppServlets.customMessageServlet("up-OK"))
                .start();
    }

    @AfterClass
    public static void stopConnectAddOn() throws Exception
    {
        if (runner != null)
        {
            runner.stopAndUninstall();
        }
    }

    @Test
    public void testViewIssuePageWithArbitraryDataInUrl() throws Exception
    {
        RemoteIssue issue = jiraOps.createIssue(project.getKey(), "Test issue for panel");
        JiraViewIssuePage viewIssuePage = product.visit(JiraViewIssuePage.class, issue.getKey());
        RemoteWebPanel panel = viewIssuePage.findWebPanel(getModuleKey(runner, ISSUE_PANEL_LEFT2_KEY)).waitUntilContentLoaded();

        assertEquals(issue.getId(), panel.getFromQueryString("my-issue-id"));
        assertEquals(project.getId(), panel.getFromQueryString("my-project-id"));

        assertEquals("ilwp2-OK", panel.getCustomMessage());
    }

    @Test
    public void testViewProjectAdminPanel() throws Exception
    {
        JiraProjectAdministrationPage projectAdministrationPage = loginAndVisit(ADMIN, JiraProjectAdministrationPage.class, project.getKey());
        RemoteWebPanel panel = projectAdministrationPage.findWebPanel(getModuleKey(runner, PROJECT_CONFIG_PANEL_KEY)).waitUntilContentLoaded();

        assertEquals(project.getId(), panel.getProjectId());
        assertEquals(ADMIN_USERNAME, panel.getUserId());
        assertNotNull(panel.getUserKey());

        assertEquals(project.getId(), panel.getProjectId());
        assertEquals(ADMIN_USERNAME, panel.getUserId());
        assertNotNull(panel.getUserKey());

        assertEquals("pcp-OK", panel.getCustomMessage());
    }

    @Test
    public void testLeftWebPanelOnIssuePage() throws RemoteException
    {
        login(ADMIN);
        RemoteIssue issue = jiraOps.createIssue(project.getKey(), "Test issue for left remotable-web-panel panel");
        JiraViewIssuePage page = product.visit(JiraViewIssuePage.class, issue.getKey());
        RemoteWebPanel panel = page.findWebPanel(getModuleKey(runner, ISSUE_PANEL_LEFT_KEY)).waitUntilContentLoaded();

        assertEquals(project.getId(), panel.getProjectId());
        assertEquals(issue.getId(), panel.getIssueId());
        assertEquals(ADMIN_USERNAME, panel.getUserId());
        assertNotNull(panel.getUserKey());

        assertEquals("ilwp-OK", panel.getCustomMessage());
    }

    @Test
    public void testRightWebPanelOnIssuePage() throws RemoteException
    {
        login(ADMIN);
        RemoteIssue issue = jiraOps.createIssue(project.getKey(), "Another test issue for right remotable-web-panel panel");
        JiraViewIssuePage page = product.visit(JiraViewIssuePage.class, issue.getKey());
        RemoteWebPanel panel = page.findWebPanel(getModuleKey(runner, ISSUE_PANEL_RIGHT_KEY)).waitUntilContentLoaded();

        assertEquals(project.getId(), panel.getProjectId());
        assertEquals(issue.getId(), panel.getIssueId());
        assertEquals(ADMIN_USERNAME, panel.getUserId());
        assertNotNull(panel.getUserKey());

        assertEquals("irwp-OK", panel.getCustomMessage());
    }

    @Test
    public void testWebPanelInProjectHeader()
    {
        JiraProjectAdministrationPage projectAdministrationPage = loginAndVisit(ADMIN, JiraProjectAdministrationPage.class, project.getKey());
        RemoteWebPanel panel = projectAdministrationPage.findWebPanel(getModuleKey(runner, PROJECT_CONFIG_HEADER_KEY)).waitUntilContentLoaded();

        assertEquals(project.getId(), panel.getProjectId());
        assertEquals(ADMIN_USERNAME, panel.getUserId());
        assertNotNull(panel.getUserKey());

        assertEquals("pch-OK", panel.getCustomMessage());
    }

    @Test
    public void testWebPanelInUserProfile()
    {
        final String userProfileName = "barney";

        JiraViewProfilePage jiraViewProfilePage = loginAndVisit(ADMIN, JiraViewProfilePage.class, userProfileName);
        RemoteWebPanel panel = jiraViewProfilePage.findWebPanel(getModuleKey(runner, USER_PROFILE_KEY)).waitUntilContentLoaded();

        assertEquals(userProfileName, panel.getFromQueryString("profile_user_key"));
        assertEquals(userProfileName, panel.getFromQueryString("profile_user_name"));
        assertEquals(ADMIN_USERNAME, panel.getUserId());
        assertNotNull(panel.getUserKey());

        assertEquals("up-OK", panel.getCustomMessage());
    }
}

