package it.jira;

import com.atlassian.jira.pageobjects.dialogs.ShifterDialog;
import com.atlassian.jira.pageobjects.navigator.AdvancedSearch;
import com.atlassian.jira.plugin.issuenav.pageobjects.IssueDetailPage;
import com.atlassian.plugin.connect.api.xmldescriptor.XmlDescriptor;
import com.atlassian.plugin.connect.modules.beans.WebItemTargetType;
import com.atlassian.plugin.connect.modules.beans.nested.I18nProperty;
import com.atlassian.plugin.connect.modules.beans.nested.ScopeName;
import com.atlassian.plugin.connect.plugin.ConnectPluginInfo;
import com.atlassian.plugin.connect.test.AddonTestUtils;
import com.atlassian.plugin.connect.test.pageobjects.ConnectAddOnEmbeddedTestPage;
import com.atlassian.plugin.connect.test.pageobjects.ConnectAddOnTestPage;
import com.atlassian.plugin.connect.test.pageobjects.RemotePluginDialog;
import com.atlassian.plugin.connect.test.pageobjects.jira.JiraAdministrationHomePage;
import com.atlassian.plugin.connect.test.pageobjects.jira.JiraViewIssuePageWithRemotePluginIssueTab;
import com.atlassian.plugin.connect.test.pageobjects.jira.PlainTextView;
import com.atlassian.plugin.connect.test.pageobjects.jira.ViewChangingSearchResult;
import com.atlassian.plugin.connect.test.server.ConnectRunner;
import hudson.plugins.jira.soap.RemoteIssue;
import it.servlet.ConnectAppServlets;
import org.junit.*;

import java.rmi.RemoteException;
import java.util.concurrent.Callable;

import static com.atlassian.plugin.connect.modules.beans.ConnectPageModuleBean.newPageBean;
import static com.atlassian.plugin.connect.modules.beans.ConnectTabPanelModuleBean.newTabPanelBean;
import static com.atlassian.plugin.connect.modules.beans.WebItemModuleBean.newWebItemBean;
import static com.atlassian.plugin.connect.modules.beans.WebItemTargetBean.newWebItemTargetBean;
import static it.TestConstants.ADMIN_FULL_NAME;
import static org.junit.Assert.*;

public class TestJira extends JiraWebDriverTestBase
{
    public static final String EXTRA_PREFIX = "servlet-";
    private static ConnectRunner remotePlugin;

    @BeforeClass
    public static void startConnectAddOn() throws Exception
    {
        remotePlugin = new ConnectRunner(product.getProductInstance().getBaseUrl(), AddonTestUtils.randomAddOnKey())
                .setAuthenticationToNone()
                .addModules("adminPages",
                        newPageBean()
                                .withKey("remotePluginAdmin")
                                .withName(new I18nProperty("Remotable Plugin app1 Admin", "admin.page.app1"))
                                .withUrl("/ap")
                                .build(),
                        newPageBean()
                                .withKey("jira-admin-page")
                                .withName(new I18nProperty("Remotable Admin Page", "admin.page"))
                                .withUrl("/jap")
                                .withLocation("advanced_menu_section/advanced_section")
                                .build())
                .addRoute("/ap", ConnectAppServlets.apRequestServlet())
                .addRoute("/jap", ConnectAppServlets.apRequestServlet())
                .addModule("jiraIssueTabPanels",
                        newTabPanelBean()
                                .withKey("jira-remotePluginIssueTabPage")
                                .withName(new I18nProperty("AC Play Issue Tab Page", "issue.tab"))
                                .withUrl("/itp")
                                .build())
                .addRoute("/itp", ConnectAppServlets.apRequestServlet())
                .addModule("webItems",
                        newWebItemBean()
                                .withKey("jira-issueAction")
                                .withName(new I18nProperty("Test Issue Action", "issue.action"))
                                .withUrl("/jia")
                                .withLocation("operations-subtasks")
                                .withTarget(newWebItemTargetBean().withType(WebItemTargetType.dialog).build())
                                .build())
                .addRoute("/jia", ConnectAppServlets.dialogServlet())
                .addScope(ScopeName.READ)
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
    @Ignore("partly ported from xml to json: see comments")
    @XmlDescriptor(comment="partly ported from xml to json: see comments")
    public void testLoadDialogFromIssueNavigatorActionCog() throws RemoteException
    {
        loginAsAdmin();
        // ensure one issue
        RemoteIssue issue = jiraOps.createIssue(project.getKey(), "Test issue for dialog action cog test");

        final ShifterDialog shifterDialog = product.getPageBinder()
                .navigateToAndBind(IssueDetailPage.class, issue.getKey())
                .details()
                .openFocusShifter();
        // TODO: select the "Test Issue Action" text (a link with id="<add-on key>__jira-issue-action"),
        // which causes the iframe to be loaded inside a container div with id="embedded-<add-on key>__jira-issue-action",
        // and then look for iframe content by binding to the iframe and calling RemotePluginDialog.wasSubmitted() etc
        ConnectAddOnTestPage page1 = shifterDialog.queryAndSelect("Test Issue Action", ConnectAddOnTestPage.class, "jira-issue-action", remotePlugin.getAddon().getKey(), false);
        ConnectAddOnTestPage page2 = product.getPageBinder().bind(ConnectAddOnTestPage.class, "jira-issue-action", remotePlugin.getAddon().getKey(), true);

        RemotePluginDialog dialog = product.getPageBinder().bind(RemotePluginDialog.class, page2);

        assertFalse(dialog.wasSubmitted());
        assertEquals(false, dialog.submit());
        assertTrue(dialog.wasSubmitted());
        assertEquals(true, dialog.submit());
    }

    @Test
    @Ignore("partly ported from xml to json: see comments")
    @XmlDescriptor(comment="partly ported from xml to json: see comments")
    public void testViewIssueTab() throws Exception
    {
        testLoggedInAndAnonymous(new Callable()
        {
            @Override
            public Object call() throws Exception
            {
                RemoteIssue issue = jiraOps.createIssue(project.getKey(), "Test issue for tab");
                String addOnKey = remotePlugin.getAddon().getKey();
                // TODO: click the link with id="<add-on key>__jira-remote-plugin-issue-tab-page",
                // which loads the iframe contained in the div with id="embedded-<add-on key>__jira-remote-plugin-issue-tab-page",
                // and then look for iframe content with JiraViewIssuePageWithRemotePluginIssueTab.getMessage()
                //JiraViewIssuePageWithRemotePluginIssueTab page = product.visit(
                //        JiraViewIssuePageWithRemotePluginIssueTab.class, issue.getKey(), addOnKey, addOnKey + ":");
                JiraViewIssuePageWithRemotePluginIssueTab page = product.visit(
                        JiraViewIssuePageWithRemotePluginIssueTab.class, "issue-tab-panel", issue.getKey(), addOnKey, ConnectPluginInfo.getPluginKey() + ":");
                Assert.assertEquals("Success", page.getMessage());
                return null;
            }
        });
    }

    @Test
    @Ignore("This test breaks with JIRA 5.2 because of stale page objects. I'm not sure what it tests at all (if anything) so disabling for now.")
    public void testSearchRequestViewPage() throws Exception
    {
        testLoggedInAndAnonymous(new Callable()
        {
            @Override
            public Object call() throws Exception
            {
                RemoteIssue issue = jiraOps.createIssue(project.getKey(), "Test issue for tab");
                product.visit(AdvancedSearch.class).enterQuery("project = " + project.getKey()).submit();

                PlainTextView plainTextView = product.getPageBinder()
                                                     .bind(ViewChangingSearchResult.class)
                                                     .openView("Raw Keys", PlainTextView.class);
                assertTrue(plainTextView.getContent().contains(issue.getKey()));
                return null;
            }
        });
    }

    @Test
    public void testAdminPageInJiraSpecificLocation() throws Exception
    {
        loginAsAdmin();
        final JiraAdministrationHomePage adminPage = product.visit(JiraAdministrationHomePage.class, EXTRA_PREFIX, remotePlugin.getAddon().getKey());
        assertTrue(adminPage.hasJiraRemotableAdminPageLink());
        assertEquals(ADMIN_FULL_NAME, adminPage.clickJiraRemotableAdminPage().getFullName());
    }

    @Test
    public void testGeneralAdminPage() throws Exception
    {
        loginAsAdmin();
        final JiraAdministrationHomePage adminPage = product.visit(JiraAdministrationHomePage.class, EXTRA_PREFIX, remotePlugin.getAddon().getKey());
        assertTrue(adminPage.hasGeneralRemotableAdminPage());
        final ConnectAddOnEmbeddedTestPage nextPage = adminPage.clickGeneralRemotableAdminPage();
        assertEquals(ADMIN_FULL_NAME, nextPage.getFullName());
    }
}
