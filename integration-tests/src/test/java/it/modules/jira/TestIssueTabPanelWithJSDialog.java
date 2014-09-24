package it.modules.jira;

import com.atlassian.jira.functest.framework.FunctTestConstants;
import com.atlassian.jira.tests.TestBase;
import com.atlassian.plugin.connect.modules.beans.nested.I18nProperty;
import com.atlassian.plugin.connect.modules.beans.nested.ScopeName;
import com.atlassian.plugin.connect.plugin.ConnectPluginInfo;
import com.atlassian.plugin.connect.plugin.capabilities.provider.ConnectTabPanelModuleProvider;
import com.atlassian.plugin.connect.test.AddonTestUtils;
import com.atlassian.plugin.connect.test.pageobjects.RemoteCloseDialogPage;
import com.atlassian.plugin.connect.test.pageobjects.RemoteDialogOpeningPage;
import com.atlassian.plugin.connect.test.pageobjects.jira.JiraOps;
import com.atlassian.plugin.connect.test.pageobjects.jira.JiraViewIssuePageWithRemotePluginIssueTab;
import com.atlassian.plugin.connect.test.server.ConnectRunner;
import it.servlet.ConnectAppServlets;
import org.junit.*;

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
public class TestIssueTabPanelWithJSDialog extends TestBase
{
    private static final String PLUGIN_KEY = AddonTestUtils.randomAddOnKey();
    private static final String ISSUE_TAB_PANEL_W_DIALOG = "issue-tab-panel-w-dialog";
    private static JiraOps jiraOps = new JiraOps(jira().getProductInstance());

    private static ConnectRunner remotePlugin;
    private static final String PROJECT_KEY = FunctTestConstants.PROJECT_HOMOSAP_KEY;
    private String issueKey;
    private static final String ADDON_DIALOG = "my-dialog";
    private static final String ADDON_DIALOG_NAME = "my dialog";

    @BeforeClass
    public static void startConnectAddOn() throws Exception
    {
        remotePlugin = new ConnectRunner(jira().getProductInstance().getBaseUrl(), PLUGIN_KEY)
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
        backdoor().project().addProject(PROJECT_KEY, PROJECT_KEY, "admin");

        issueKey = jiraOps.createIssue(PROJECT_KEY, "Test issue for tab").getKey();
    }

    @After
    public void cleanUpTest()
    {
        backdoor().project().deleteProject(PROJECT_KEY);
    }

    @Test
    public void testIssueTabPanelWithJSDialog() throws RemoteException
    {
        jira().gotoLoginPage().loginAsSysadminAndGoToHome();
        JiraViewIssuePageWithRemotePluginIssueTab page = jira().visit(
                JiraViewIssuePageWithRemotePluginIssueTab.class, ISSUE_TAB_PANEL_W_DIALOG, issueKey, PLUGIN_KEY, ConnectPluginInfo.getPluginKey() + ":");

        RemoteDialogOpeningPage dialogOpeningPage = jira().getPageBinder().bind(RemoteDialogOpeningPage.class, addonAndModuleKey(PLUGIN_KEY,ISSUE_TAB_PANEL_W_DIALOG));
        RemoteCloseDialogPage closeDialogPage = dialogOpeningPage.openKey(addonAndModuleKey(PLUGIN_KEY,ADDON_DIALOG));

        assertThat(closeDialogPage.getFromQueryString("myproject_key"), is(PROJECT_KEY));
        assertThat(closeDialogPage.getFromQueryString("myissue_key"), is(issueKey));

        closeDialogPage.close();
        closeDialogPage.waitUntilClosed();
        String response = dialogOpeningPage.waitForValue("dialog-close-data");
        assertEquals("test dialog close data", response);
    }
}
