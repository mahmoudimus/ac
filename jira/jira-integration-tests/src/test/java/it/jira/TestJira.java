package it.jira;

import com.atlassian.connect.test.jira.pageobjects.JiraViewIssuePageWithRemotePluginIssueTab;
import com.atlassian.jira.pageobjects.dialogs.ShifterDialog;
import com.atlassian.jira.pageobjects.pages.admin.configuration.ViewGeneralConfigurationPage;
import com.atlassian.jira.plugin.issuenav.pageobjects.IssueDetailPage;
import com.atlassian.jira.rest.api.issue.IssueCreateResponse;
import com.atlassian.plugin.connect.modules.beans.WebItemTargetType;
import com.atlassian.plugin.connect.modules.beans.nested.I18nProperty;
import com.atlassian.plugin.connect.modules.beans.nested.ScopeName;
import com.atlassian.plugin.connect.modules.util.ModuleKeyUtils;
import com.atlassian.plugin.connect.test.common.pageobjects.ConnectAddonEmbeddedTestPage;
import com.atlassian.plugin.connect.test.common.pageobjects.RemotePluginDialog;
import com.atlassian.plugin.connect.test.common.pageobjects.RemoteWebItem;
import com.atlassian.plugin.connect.test.common.servlet.ConnectAppServlets;
import com.atlassian.plugin.connect.test.common.servlet.ConnectRunner;
import com.atlassian.plugin.connect.test.common.util.AddonTestUtils;
import com.atlassian.plugin.connect.test.common.util.TestUser;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.rmi.RemoteException;
import java.util.Optional;

import static com.atlassian.plugin.connect.modules.beans.ConnectPageModuleBean.newPageBean;
import static com.atlassian.plugin.connect.modules.beans.ConnectTabPanelModuleBean.newTabPanelBean;
import static com.atlassian.plugin.connect.modules.beans.WebItemModuleBean.newWebItemBean;
import static com.atlassian.plugin.connect.modules.beans.WebItemTargetBean.newWebItemTargetBean;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

public class TestJira extends JiraWebDriverTestBase {
    private static final String ADMIN_KEY = "addon-admin";
    private static final String ADVANCED_ADMIN_KEY = "advanced-addon-admin";
    private static final String ISSUE_TAB_PANEL_KEY = "issue-tab-panel";
    private static final String JIRA_ISSUE_ACTION_KEY = "jira-issue-action";

    private static ConnectRunner runner;

    @BeforeClass
    public static void startConnectAddon() throws Exception {
        runner = new ConnectRunner(product.getProductInstance().getBaseUrl(), AddonTestUtils.randomAddonKey())
                .setAuthenticationToNone()
                .addModules("adminPages",
                        newPageBean()
                                .withKey(ADMIN_KEY)
                                .withName(new I18nProperty("Addon Admin", null))
                                .withUrl("/admin")
                                .build(),
                        newPageBean()
                                .withKey(ADVANCED_ADMIN_KEY)
                                .withName(new I18nProperty("Addon Advanced Admin", null))
                                .withUrl("/advanced-admin")
                                .withLocation("advanced_menu_section/advanced_section")
                                .build())
                .addRoute("/admin", ConnectAppServlets.apRequestServlet())
                .addRoute("/advanced-admin", ConnectAppServlets.apRequestServlet())
                .addModule("jiraIssueTabPanels",
                        newTabPanelBean()
                                .withKey(ISSUE_TAB_PANEL_KEY)
                                .withName(new I18nProperty("AC Play Issue Tab Page", null))
                                .withUrl("/issue-tab-panel")
                                .build())
                .addRoute("/issue-tab-panel", ConnectAppServlets.apRequestServlet())
                .addModule("webItems",
                        newWebItemBean()
                                .withKey(JIRA_ISSUE_ACTION_KEY)
                                .withName(new I18nProperty("Test Issue Action", null))
                                .withUrl("/jia")
                                .withLocation("operations-subtasks")
                                .withTarget(newWebItemTargetBean().withType(WebItemTargetType.dialog).build())
                                .build())
                .addRoute("/jia", ConnectAppServlets.dialogServlet())
                .addScope(ScopeName.READ)
                .start();
    }

    @AfterClass
    public static void stopConnectAddon() throws Exception {
        if (runner != null) {
            runner.stopAndUninstall();
        }
    }

    @Test
    public void testLoadDialogFromIssueNavigatorActionCog() throws RemoteException {
        TestUser user = testUserFactory.basicUser();
        login(user);
        // ensure one issue
        IssueCreateResponse issue = product.backdoor().issues().createIssue(project.getKey(), "Test issue for dialog action cog test");

        final ShifterDialog shifterDialog = product.getPageBinder()
                .navigateToAndBind(IssueDetailPage.class, issue.key)
                .details()
                .openFocusShifter();
        ConnectAddonEmbeddedTestPage page = shifterDialog.queryAndSelect("Test Issue Action", ConnectAddonEmbeddedTestPage.class, runner.getAddon().getKey(), JIRA_ISSUE_ACTION_KEY, true);
        RemotePluginDialog dialog = product.getPageBinder().bind(RemotePluginDialog.class, page);

        assertFalse(dialog.wasSubmitted());
        dialog.submitAndWaitUntilSubmitted();
        dialog.submitAndWaitUntilHidden();
    }

    @Test
    public void testViewIssueTab() throws Exception {
        login(testUserFactory.basicUser());

        IssueCreateResponse issue = product.backdoor().issues().createIssue(project.getKey(), "Test issue for tab");
        String addonKey = runner.getAddon().getKey();
        JiraViewIssuePageWithRemotePluginIssueTab page = product.visit(
                JiraViewIssuePageWithRemotePluginIssueTab.class, ISSUE_TAB_PANEL_KEY, issue.key, addonKey);
        Assert.assertEquals("Success", page.getMessage());
    }

    @Test
    public void testAdminPageInJiraSpecificLocation() throws Exception {
        TestUser user = testUserFactory.admin();
        String addonKey = runner.getAddon().getKey();
        loginAndVisit(user, ViewGeneralConfigurationPage.class);

        RemoteWebItem adminPageLink = getAdminPageLink(addonKey, ADVANCED_ADMIN_KEY);

        adminPageLink.click();

        ConnectAddonEmbeddedTestPage nextPage = connectPageOperations.getPageBinder().bind(ConnectAddonEmbeddedTestPage.class, addonKey, ADVANCED_ADMIN_KEY, true);
        assertEquals(user.getDisplayName(), nextPage.getFullName());
    }

    @Test
    public void testGeneralAdminPage() throws Exception {
        TestUser user = testUserFactory.admin();
        String addonKey = runner.getAddon().getKey();
        loginAndVisit(user, ViewGeneralConfigurationPage.class);

        RemoteWebItem adminPageLink = getAdminPageLink(addonKey, ADMIN_KEY);
        adminPageLink.click();

        ConnectAddonEmbeddedTestPage nextPage = connectPageOperations.getPageBinder().bind(ConnectAddonEmbeddedTestPage.class, addonKey, ADMIN_KEY, true);
        assertEquals(user.getDisplayName(), nextPage.getFullName());
    }

    private RemoteWebItem getAdminPageLink(String addonKey, String adminPageWebItemKey) {
        String webitemId = ModuleKeyUtils.addonAndModuleKey(addonKey, adminPageWebItemKey);
        return connectPageOperations.findWebItem(RemoteWebItem.ItemMatchingMode.ID, webitemId, Optional.<String>empty());
    }
}
