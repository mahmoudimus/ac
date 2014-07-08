package it.jira;

import com.atlassian.jira.functest.framework.FunctTestConstants;
import com.atlassian.jira.testkit.client.restclient.Version;
import com.atlassian.jira.testkit.client.restclient.VersionClient;
import com.atlassian.jira.tests.TestBase;
import com.atlassian.plugin.connect.modules.beans.nested.I18nProperty;
import com.atlassian.plugin.connect.plugin.ConnectPluginInfo;
import com.atlassian.plugin.connect.plugin.capabilities.provider.ConnectTabPanelModuleProvider;
import com.atlassian.plugin.connect.test.AddonTestUtils;
import com.atlassian.plugin.connect.test.pageobjects.jira.JiraVersionTabPage;
import com.atlassian.plugin.connect.test.server.ConnectRunner;
import it.servlet.ConnectAppServlets;
import org.junit.*;

import java.rmi.RemoteException;

import static com.atlassian.plugin.connect.modules.beans.ConnectTabPanelModuleBean.newTabPanelBean;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

/**
 * Test of remote version tab panel in JIRA
 */
public class TestVersionTabPage extends TestBase
{
    private static ConnectRunner remotePlugin;
    private static String moduleKey;

    private static final String PROJECT_KEY = FunctTestConstants.PROJECT_HOMOSAP_KEY;
    private static final String VERSION_NAME = "2.7.1";
    private String versionId;


    @BeforeClass
    public static void startConnectAddOn() throws Exception
    {
        final String route = "/ipp?version_id=${version.id}&project_id=${project.id}&project_key=${project.key}";
        moduleKey = AddonTestUtils.randomModuleKey();
        remotePlugin = new ConnectRunner(jira().environmentData().getBaseUrl().toString(), AddonTestUtils.randomAddOnKey())
                .addJWT()
                .addInstallLifecycle()
                .addRoute(ConnectRunner.INSTALLED_PATH, ConnectAppServlets.installHandlerServlet())
                .addModule(ConnectTabPanelModuleProvider.VERSION_TAB_PANELS, newTabPanelBean()
                        .withName(new I18nProperty("Version Tab Panel", "my.versionTabPanel"))
                        .withKey(moduleKey)
                        .withUrl(route)
                        .withWeight(100)
                        .build())
                .addRoute(route, ConnectAppServlets.apRequestServlet())
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
        final VersionClient versionClient = new VersionClient(jira().environmentData());
        versionId = Long.toString(versionClient.create(new Version().name(VERSION_NAME + System.currentTimeMillis()).project(PROJECT_KEY)).id);
    }

    @After
    public void cleanUpTest()
    {
        backdoor().project().deleteProject(PROJECT_KEY);
    }

    @Test
    public void testVersionTabPanel() throws RemoteException
    {
        jira().gotoLoginPage().loginAsSysadminAndGoToHome();
        JiraVersionTabPage versionTabPage = jira().goTo(JiraVersionTabPage.class, PROJECT_KEY, versionId, ConnectPluginInfo.getPluginKey(), remotePlugin.getAddon().getKey() + "__" + moduleKey);

        versionTabPage.clickTab();

        assertThat(versionTabPage.getVersionId(), equalTo(versionId));
        assertThat(versionTabPage.getProjectKey(), equalTo(PROJECT_KEY));
    }
}
