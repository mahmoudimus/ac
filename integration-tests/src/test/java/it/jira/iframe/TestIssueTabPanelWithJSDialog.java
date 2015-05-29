package it.jira.iframe;

import com.atlassian.jira.rest.api.issue.IssueCreateResponse;
import com.atlassian.plugin.connect.jira.capabilities.provider.ConnectTabPanelModuleProvider;
import com.atlassian.plugin.connect.modules.beans.nested.I18nProperty;
import com.atlassian.plugin.connect.modules.beans.nested.ScopeName;
import com.atlassian.plugin.connect.test.AddonTestUtils;
import com.atlassian.plugin.connect.test.pageobjects.RemoteCloseDialogPage;
import com.atlassian.plugin.connect.test.pageobjects.RemoteDialogOpeningPanel;
import com.atlassian.plugin.connect.test.pageobjects.jira.JiraViewIssuePageWithRemotePluginIssueTab;
import com.atlassian.plugin.connect.test.server.ConnectRunner;
import it.jira.JiraWebDriverTestBase;
import it.servlet.ConnectAppServlets;
import it.util.TestProject;
import it.util.TestUser;
import org.apache.commons.lang.RandomStringUtils;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import java.rmi.RemoteException;

import static com.atlassian.plugin.connect.modules.beans.ConnectPageModuleBean.newPageBean;
import static com.atlassian.plugin.connect.modules.beans.ConnectTabPanelModuleBean.newTabPanelBean;
import static com.atlassian.plugin.connect.modules.util.ModuleKeyUtils.addonAndModuleKey;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertEquals;

/**
 * Test of remote issue tab panel in JIRA
 */
public class TestIssueTabPanelWithJSDialog extends JiraWebDriverTestBase
{
    private static final String PLUGIN_KEY = AddonTestUtils.randomAddOnKey();
    private static final String ISSUE_TAB_PANEL_W_DIALOG = "issue-tab-panel-w-dialog";

    private static ConnectRunner remotePlugin;
    private static final String ADDON_DIALOG = "my-dialog";
    private static final String ADDON_DIALOG_NAME = "my dialog";

    private TestUser user;
    private IssueCreateResponse issue;
    private TestProject project;

    @BeforeClass
    public static void startConnectAddOn() throws Exception
    {
        product.logout();

        remotePlugin = new ConnectRunner(product.getProductInstance().getBaseUrl(), PLUGIN_KEY)
                .setAuthenticationToNone()
                .addModules(ConnectTabPanelModuleProvider.ISSUE_TAB_PANELS,
                        newTabPanelBean()
                                .withName(new I18nProperty("Issue Tab Panel W Dialog", null))
                                .withKey(ISSUE_TAB_PANEL_W_DIALOG)
                                .withUrl("/ippd?issue_id={issue.id}&project_id={project.id}&project_key={project.key}")
                                .withWeight(1234)
                                .build()
                )
                .addModule("generalPages",
                        // this general page is so that we'll have a servlet backing the dialog which we create via js
                        newPageBean()
                                .withName(new I18nProperty(ADDON_DIALOG_NAME, null))
                                .withUrl("/my-dialog-url?myproject_key={project.key}&myissue_key={issue.key}")
                                .withKey(ADDON_DIALOG)
                                .build()
                )
                .addRoute("/ippd", ConnectAppServlets.openDialogServlet())
                .addRoute("/my-dialog-url", ConnectAppServlets.closeDialogServlet())
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

    @Before
    public void setUpTest() throws Exception
    {
        user = testUserFactory.basicUser();
        String projectKey = RandomStringUtils.randomAlphabetic(4).toUpperCase();
        String projectId = String.valueOf(product.backdoor().project().addProject(projectKey, projectKey, user.getUsername()));
        project = new TestProject(projectKey, projectId);
        issue = product.backdoor().issues().createIssue(project.getKey(), "Test issue for tab", user.getUsername());
    }

    @After
    public void cleanUpTest()
    {
        product.backdoor().project().deleteProject(project.getKey());
    }

    @Test
    public void testIssueTabPanelWithJSDialog() throws RemoteException
    {
        TestUser user = testUserFactory.basicUser();
        product.quickLogin(user.getUsername(), user.getPassword());
        product.visit(JiraViewIssuePageWithRemotePluginIssueTab.class, ISSUE_TAB_PANEL_W_DIALOG, issue.key(), PLUGIN_KEY);

        RemoteDialogOpeningPanel dialogOpeningPage = product.getPageBinder().bind(RemoteDialogOpeningPanel.class, addonAndModuleKey(PLUGIN_KEY, ISSUE_TAB_PANEL_W_DIALOG));
        RemoteCloseDialogPage closeDialogPage = dialogOpeningPage.openKey(addonAndModuleKey(PLUGIN_KEY, ADDON_DIALOG));

        assertThat(closeDialogPage.getFromQueryString("myproject_key"), is(project.getKey()));
        assertThat(closeDialogPage.getFromQueryString("myissue_key"), is(issue.key()));

        closeDialogPage.close();
        closeDialogPage.waitUntilClosed();
        String response = dialogOpeningPage.waitForValue("dialog-close-data");
        assertEquals("test dialog close data", response);
    }
}
