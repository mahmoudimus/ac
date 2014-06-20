package it.jira;

import com.atlassian.jira.functest.framework.FunctTestConstants;
import com.atlassian.jira.testkit.client.restclient.Version;
import com.atlassian.jira.testkit.client.restclient.VersionClient;
import com.atlassian.jira.tests.TestBase;
import com.atlassian.plugin.connect.api.xmldescriptor.XmlDescriptor;
import com.atlassian.plugin.connect.plugin.module.jira.versiontab.VersionTabPageModuleDescriptor;
import com.atlassian.plugin.connect.test.pageobjects.jira.JiraVersionTabPage;
import com.atlassian.plugin.connect.test.server.AtlassianConnectAddOnRunner;
import com.atlassian.plugin.connect.test.server.module.VersionTabPageModule;
import it.servlet.ConnectAppServlets;
import org.junit.*;

import java.rmi.RemoteException;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

/**
 * Test of remote version tab panel in JIRA
 */
@XmlDescriptor
public class TestVersionTabPage extends TestBase
{
    private static AtlassianConnectAddOnRunner remotePlugin;

    private static final String PROJECT_KEY = FunctTestConstants.PROJECT_HOMOSAP_KEY;
    private static final String MODULE_KEY = "jira-version-tab-panel";
    private static final String ACTUAL_MODULE_KEY = VersionTabPageModuleDescriptor.VERSION_TAB_PAGE_MODULE_PREFIX + MODULE_KEY;

    private static final String VERSION_NAME = "2.7.1";
    private String versionId;


    @BeforeClass
    public static void startConnectAddOn() throws Exception
    {
        remotePlugin = new AtlassianConnectAddOnRunner(jira().environmentData().getBaseUrl().toString())
                .addOAuth()
                .add(VersionTabPageModule.key(MODULE_KEY)
                        .name("Version Tab Panel")
                        .path("/ipp?version_id=${version.id}&project_id=${project.id}&project_key=${project.key}")
                        .resource(ConnectAppServlets.apRequestServlet()))
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
        JiraVersionTabPage versionTabPage = jira().goTo(JiraVersionTabPage.class, PROJECT_KEY, versionId, remotePlugin.getPluginKey(), ACTUAL_MODULE_KEY);

        versionTabPage.clickTab();

        assertThat(versionTabPage.getVersionId(), equalTo(versionId));
        assertThat(versionTabPage.getProjectKey(), equalTo(PROJECT_KEY));
    }
}
