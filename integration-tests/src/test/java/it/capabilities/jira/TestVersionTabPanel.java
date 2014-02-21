package it.capabilities.jira;

import com.atlassian.jira.functest.framework.FunctTestConstants;
import com.atlassian.jira.testkit.client.restclient.Version;
import com.atlassian.jira.testkit.client.restclient.VersionClient;
import com.atlassian.jira.tests.TestBase;
import com.atlassian.plugin.connect.modules.beans.nested.I18nProperty;
import com.atlassian.plugin.connect.plugin.capabilities.provider.ConnectTabPanelModuleProvider;
import com.atlassian.plugin.connect.test.pageobjects.jira.JiraVersionTabPage;
import com.atlassian.plugin.connect.test.server.ConnectRunner;
import it.servlet.ConnectAppServlets;
import org.junit.*;
import org.junit.rules.TestRule;

import java.rmi.RemoteException;

import static com.atlassian.plugin.connect.modules.beans.ConnectTabPanelModuleBean.newTabPanelBean;
import static it.servlet.condition.ToggleableConditionServlet.toggleableConditionBean;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

/**
 * Test of remote version tab panel in JIRA
 */
public class TestVersionTabPanel extends TestBase
{
    private static final String PLUGIN_KEY = "my-plugin";
    private static final String MODULE_KEY = "version-tab-panel";
    private static final String PROJECT_KEY = FunctTestConstants.PROJECT_HOMOSAP_KEY;

    private static ConnectRunner remotePlugin;

    private String versionId;
    private static final String VERSION_NAME = "2.7.1";

    @Rule
    public TestRule resetToggleableCondition = remotePlugin.resetToggleableConditionRule();

    @BeforeClass
    public static void startConnectAddOn() throws Exception
    {
        remotePlugin = new ConnectRunner(jira().getProductInstance().getBaseUrl(), PLUGIN_KEY)
                .setAuthenticationToNone()
                .addModule(ConnectTabPanelModuleProvider.VERSION_TAB_PANELS, newTabPanelBean()
                        .withName(new I18nProperty("Version Tab Panel", null))
                        .withKey(MODULE_KEY)
                        .withUrl("/ipp?version_id={version.id}&project_id={project.id}&project_key={project.key}")
                        .withWeight(1234)
                        .withConditions(toggleableConditionBean())
                        .build())
                .addRoute("/ipp", ConnectAppServlets.apRequestServlet())
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
        final JiraVersionTabPage versionTabPage = jira().goTo(JiraVersionTabPage.class, PROJECT_KEY, versionId, PLUGIN_KEY, MODULE_KEY);

        assertThat("The addon tab should be present", versionTabPage.isAddOnTabPresent(), is(true));
        versionTabPage.clickTab();

        assertThat(versionTabPage.getVersionId(), equalTo(versionId));
        assertThat(versionTabPage.getProjectKey(), equalTo(PROJECT_KEY));
    }

    @Test
    public void tabIsNotAccessibleWithFalseCondition() throws Exception
    {
        remotePlugin.setToggleableConditionShouldDisplay(false);

        jira().gotoLoginPage().loginAsSysadminAndGoToHome();
        final JiraVersionTabPage versionTabPage = jira().goTo(JiraVersionTabPage.class, PROJECT_KEY, versionId, PLUGIN_KEY, MODULE_KEY);

        assertThat("The addon tab SHOULD NOT be present", versionTabPage.isAddOnTabPresent(), is(false));
    }
}
