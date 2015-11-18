
package it.jira.iframe;

import com.atlassian.jira.rest.api.issue.IssueCreateResponse;
import com.atlassian.plugin.connect.modules.beans.nested.I18nProperty;
import com.atlassian.plugin.connect.modules.beans.nested.WebPanelLayout;
import com.atlassian.plugin.connect.modules.util.ModuleKeyUtils;
import com.atlassian.plugin.connect.test.pageobjects.RemoteWebPanel;
import com.atlassian.plugin.connect.test.pageobjects.jira.JiraProjectAdministrationPage;
import com.atlassian.plugin.connect.test.pageobjects.jira.JiraViewIssuePage;
import com.atlassian.plugin.connect.test.pageobjects.jira.JiraViewProfilePage;
import com.atlassian.plugin.connect.test.pageobjects.jira.JiraViewProjectPage;
import com.atlassian.plugin.connect.test.server.ConnectRunner;
import it.jira.JiraWebDriverTestBase;
import it.servlet.ConnectAppServlets;
import it.util.TestUser;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.rmi.RemoteException;

import static com.atlassian.plugin.connect.modules.beans.WebPanelModuleBean.newWebPanelBean;
import static it.modules.ConnectAsserts.verifyIframeURLHasVersionNumber;
import static it.servlet.condition.ToggleableConditionServlet.toggleableConditionBean;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;

/**
 * Test of remote web panels in JIRA.
 */
public final class TestWebPanel extends JiraWebDriverTestBase
{
    // web panel keys
    private static final String ISSUE_PANEL_LEFT_KEY = "jira-issue-left-web-panel";
    private static final String ISSUE_PANEL_LEFT2_KEY = "jira-issue-left-web-panel-2";
    private static final String ISSUE_PANEL_RIGHT_KEY = "jira-issue-right-web-panel";
    private static final String USER_PROFILE_KEY = "user-profile-web-panel";
    private static final String PROJECT_CONFIG_HEADER_KEY = "jira-project-config-header-web-panel";
    private static final String PROJECT_CONFIG_PANEL_KEY = "web-panel-project-config";
    private static final String WEB_PANEL_WITH_CONDITION_KEY = "hip-chat-discussions";

    private static ConnectRunner runner;

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @BeforeClass
    public static void startConnectAddOn() throws Exception
    {
        product.quickLoginAsAdmin();

        runner = new ConnectRunner(product)
                .setAuthenticationToNone()
                .addModules(
                        "webPanels",
                        newWebPanelBean()
                                .withKey(PROJECT_CONFIG_PANEL_KEY)
                                .withName(new I18nProperty("Project Config Panel", null))
                                .withLocation("webpanels.admin.summary.right-panels")
                                .withUrl("/pcp?issue_id=${issue.id}&project_id=${project.id}")
                                .build(),
                        newWebPanelBean()
                                .withKey(ISSUE_PANEL_LEFT_KEY)
                                .withName(new I18nProperty("Issue Left Web Panel", null))
                                .withLocation("atl.jira.view.issue.left.context")
                                .withUrl("/ilwp?issue_id=${issue.id}&project_id=${project.id}")
                                .build(),
                        newWebPanelBean()
                                .withKey(ISSUE_PANEL_LEFT2_KEY)
                                .withName(new I18nProperty("Issue Left Web Panel 2", null))
                                .withLocation("atl.jira.view.issue.left.context")
                                .withUrl("/ilwp2?my-issue-id=${issue.id}&my-project-id=${project.id}")
                                .build(),
                        newWebPanelBean()
                                .withKey(ISSUE_PANEL_RIGHT_KEY)
                                .withName(new I18nProperty("Issue Right Web Panel", null))
                                .withLocation("atl.jira.view.issue.right.context")
                                .withUrl("/irwp?issue_id=${issue.id}&project_id=${project.id}")
                                .build(),
                        newWebPanelBean()
                                .withKey(PROJECT_CONFIG_HEADER_KEY)
                                .withName(new I18nProperty("Project Config Header Web Panel", null))
                                .withLocation("atl.jira.proj.config.header")
                                .withUrl("/pch?issue_id=${issue.id}&project_id=${project.id}")
                                .build(),
                        newWebPanelBean()
                                .withKey(USER_PROFILE_KEY)
                                .withName(new I18nProperty("User Profile Web Panel", null))
                                .withLocation("webpanels.user.profile.summary.custom")
                                .withUrl("/up?profile_user_key=${profileUser.key}&profile_user_name=${profileUser.name}")
                                .build(),
                        newWebPanelBean()
                                .withName(new I18nProperty("Panel with condition", null))
                                .withKey(WEB_PANEL_WITH_CONDITION_KEY)
                                        // panel doesn't load properly as it 404s - not a prob for this test (asserts existence not content)
                                .withUrl("/cwp?projectKey={project.key}")
                                .withLocation("com.atlassian.jira.plugin.headernav.left.context")
                                .withLayout(new WebPanelLayout("100%", "200px"))
                                .withWeight(1234)
                                .withConditions(toggleableConditionBean())
                                .build()
                )
                .addRoute("/pcp", ConnectAppServlets.customMessageServlet("pcp-OK"))
                .addRoute("/ilwp", ConnectAppServlets.customMessageServlet("ilwp-OK"))
                .addRoute("/ilwp2", ConnectAppServlets.customMessageServlet("ilwp2-OK"))
                .addRoute("/irwp", ConnectAppServlets.customMessageServlet("irwp-OK"))
                .addRoute("/pch", ConnectAppServlets.customMessageServlet("pch-OK"))
                .addRoute("/up", ConnectAppServlets.customMessageServlet("up-OK"))
                .addRoute("/cwp", ConnectAppServlets.customMessageServlet("cwp-OK"))
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
        IssueCreateResponse issue = product.backdoor().issues().createIssue(project.getKey(), "Test issue for panel");
        JiraViewIssuePage viewIssuePage = product.visit(JiraViewIssuePage.class, issue.key);
        RemoteWebPanel panel = viewIssuePage.findWebPanel(getModuleKey(runner, ISSUE_PANEL_LEFT2_KEY)).waitUntilContentLoaded();

        assertEquals(issue.id, panel.getFromQueryString("my-issue-id"));
        assertEquals(project.getId(), panel.getFromQueryString("my-project-id"));

        assertEquals("ilwp2-OK", panel.getCustomMessage());

        verifyIframeURLHasVersionNumber(panel);
    }

    @Test
    public void testViewProjectAdminPanel() throws Exception
    {
        TestUser user = testUserFactory.admin();
        login(user);
        JiraProjectAdministrationPage projectAdministrationPage = product.visit(JiraProjectAdministrationPage.class, project.getKey());
        RemoteWebPanel panel = projectAdministrationPage.findWebPanel(getModuleKey(runner, PROJECT_CONFIG_PANEL_KEY)).waitUntilContentLoaded();

        assertEquals(project.getId(), panel.getProjectId());
        assertEquals(user.getUsername(), panel.getUserId());
        assertNotNull(panel.getUserKey());

        assertEquals("pcp-OK", panel.getCustomMessage());

        verifyIframeURLHasVersionNumber(panel);
    }

    @Test
    public void testLeftWebPanelOnIssuePage() throws RemoteException
    {
        TestUser user = testUserFactory.basicUser();
        login(user);
        IssueCreateResponse issue = product.backdoor().issues().createIssue(project.getKey(), "Test issue for left remotable-web-panel panel");
        JiraViewIssuePage page = product.visit(JiraViewIssuePage.class, issue.key);
        RemoteWebPanel panel = page.findWebPanel(getModuleKey(runner, ISSUE_PANEL_LEFT_KEY)).waitUntilContentLoaded();

        assertEquals(project.getId(), panel.getProjectId());
        assertEquals(issue.id, panel.getIssueId());
        assertEquals(user.getUsername(), panel.getUserId());
        assertNotNull(panel.getUserKey());

        assertEquals("ilwp-OK", panel.getCustomMessage());

        verifyIframeURLHasVersionNumber(panel);
    }

    @Test
    public void testRightWebPanelOnIssuePage() throws RemoteException
    {
        TestUser user = testUserFactory.basicUser();
        login(user);
        IssueCreateResponse issue = product.backdoor().issues().createIssue(project.getKey(), "Another test issue for right remotable-web-panel panel");
        JiraViewIssuePage page = product.visit(JiraViewIssuePage.class, issue.key);
        RemoteWebPanel panel = page.findWebPanel(getModuleKey(runner, ISSUE_PANEL_RIGHT_KEY)).waitUntilContentLoaded();

        assertEquals(project.getId(), panel.getProjectId());
        assertEquals(issue.id, panel.getIssueId());
        assertEquals(user.getUsername(), panel.getUserId());
        assertNotNull(panel.getUserKey());

        assertEquals("irwp-OK", panel.getCustomMessage());

        verifyIframeURLHasVersionNumber(panel);
    }

    @Test
    public void testWebPanelInProjectHeader()
    {
        TestUser user = testUserFactory.admin();
        login(user);
        JiraProjectAdministrationPage projectAdministrationPage = product.visit(JiraProjectAdministrationPage.class, project.getKey());
        RemoteWebPanel panel = projectAdministrationPage.findWebPanel(getModuleKey(runner, PROJECT_CONFIG_HEADER_KEY)).waitUntilContentLoaded();

        assertEquals(project.getId(), panel.getProjectId());
        assertEquals(user.getUsername(), panel.getUserId());
        assertNotNull(panel.getUserKey());

        assertEquals("pch-OK", panel.getCustomMessage());

        verifyIframeURLHasVersionNumber(panel);
    }

    @Test
    public void testWebPanelInUserProfile()
    {
        TestUser user = testUserFactory.basicUser();
        login(user);
        final String userProfileName = testUserFactory.basicUser().getUsername();
        JiraViewProfilePage jiraViewProfilePage = product.visit(JiraViewProfilePage.class, userProfileName);
        RemoteWebPanel panel = jiraViewProfilePage.findWebPanel(getModuleKey(runner, USER_PROFILE_KEY)).waitUntilContentLoaded();

        assertEquals(userProfileName, panel.getFromQueryString("profile_user_key"));
        assertEquals(userProfileName, panel.getFromQueryString("profile_user_name"));
        assertEquals(user.getUsername(), panel.getUserId());
        assertNotNull(panel.getUserKey());

        assertEquals("up-OK", panel.getCustomMessage());

        verifyIframeURLHasVersionNumber(panel);
    }

    @Test
    public void panelIsNotVisibleWithFalseCondition()
    {
        product.visit(JiraViewProjectPage.class, project.getKey());

        assertThat("AddOn web panel should be present", connectPageOperations.existsWebPanel(getModuleKey(runner, WEB_PANEL_WITH_CONDITION_KEY)), is(true));
        runner.setToggleableConditionShouldDisplay(false);

        product.visit(JiraViewProjectPage.class, project.getKey());

        assertThat("AddOn web panel should NOT be present", connectPageOperations.existsWebPanel(getModuleKey(runner, WEB_PANEL_WITH_CONDITION_KEY)), is(false));
    }

    private String getModuleKey(ConnectRunner runner, String module)
    {
        return ModuleKeyUtils.addonAndModuleKey(runner.getAddon().getKey(), module);
    }
}

