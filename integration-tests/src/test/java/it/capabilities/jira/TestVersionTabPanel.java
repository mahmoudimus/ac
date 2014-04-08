package it.capabilities.jira;

import com.atlassian.jira.functest.framework.FunctTestConstants;
import com.atlassian.jira.testkit.client.restclient.Version;
import com.atlassian.jira.testkit.client.restclient.VersionClient;
import com.atlassian.jira.tests.TestBase;
import com.atlassian.plugin.connect.modules.beans.nested.I18nProperty;
import com.atlassian.plugin.connect.modules.util.ModuleKeyUtils;
import com.atlassian.plugin.connect.plugin.ConnectPluginInfo;
import com.atlassian.plugin.connect.plugin.capabilities.provider.ConnectTabPanelModuleProvider;
import com.atlassian.plugin.connect.test.RemotePluginUtils;
import com.atlassian.plugin.connect.test.pageobjects.jira.JiraVersionTabPage;
import com.atlassian.plugin.connect.test.server.ConnectRunner;
import it.servlet.ConnectAppServlets;
import it.servlet.condition.ParameterCapturingConditionServlet;
import org.junit.*;
import org.junit.rules.TestRule;

import java.rmi.RemoteException;
import java.util.Map;

import static com.atlassian.plugin.connect.modules.beans.ConnectTabPanelModuleBean.newTabPanelBean;
import static com.atlassian.plugin.connect.modules.beans.nested.SingleConditionBean.newSingleConditionBean;
import static com.atlassian.plugin.connect.modules.util.ModuleKeyUtils.addonAndModuleKey;
import static it.servlet.condition.ParameterCapturingConditionServlet.PARAMETER_CAPTURE_URL;
import static it.servlet.condition.ToggleableConditionServlet.toggleableConditionBean;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasEntry;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

/**
 * Test of remote version tab panel in JIRA
 */
public class TestVersionTabPanel extends TestBase
{
    private static final String PLUGIN_KEY = RemotePluginUtils.randomPluginKey();
    private static final String MODULE_KEY = "version-tab-panel";
    private static final String PROJECT_KEY = FunctTestConstants.PROJECT_HOMOSAP_KEY;

    private static ConnectRunner remotePlugin;

    private long projectId;
    private String versionId;
    private static final String VERSION_NAME = "2.7.1";

    @Rule
    public TestRule resetToggleableCondition = remotePlugin.resetToggleableConditionRule();

    private static final ParameterCapturingConditionServlet PARAMETER_CAPTURING_SERVLET = new ParameterCapturingConditionServlet();

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
                        .withConditions(
                            toggleableConditionBean(),
                            newSingleConditionBean().withCondition(PARAMETER_CAPTURE_URL +
                                    "?version_id={version.id}&project_id={project.id}&project_key={project.key}").build()
                        )
                        .build())
                .addRoute("/ipp", ConnectAppServlets.apRequestServlet())
                .addRoute(PARAMETER_CAPTURE_URL, PARAMETER_CAPTURING_SERVLET)
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
        projectId = backdoor().project().addProject(PROJECT_KEY, PROJECT_KEY, "admin");
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
        final JiraVersionTabPage versionTabPage = jira().goTo(JiraVersionTabPage.class, PROJECT_KEY, versionId, ConnectPluginInfo.getPluginKey(), addonAndModuleKey(PLUGIN_KEY,MODULE_KEY));

        assertThat("The addon tab should be present", versionTabPage.isAddOnTabPresent(), is(true));
        versionTabPage.clickTab();

        assertThat(versionTabPage.getVersionId(), equalTo(versionId));
        assertThat(versionTabPage.getProjectKey(), equalTo(PROJECT_KEY));

        Map<String,String> conditionRequestParams = PARAMETER_CAPTURING_SERVLET.getParamsFromLastRequest();
        assertThat(conditionRequestParams, hasEntry("version_id", versionId));
        assertThat(conditionRequestParams, hasEntry("project_id", String.valueOf(projectId)));
        assertThat(conditionRequestParams, hasEntry("project_key", PROJECT_KEY));
    }

    @Test
    public void tabIsNotAccessibleWithFalseCondition() throws Exception
    {
        remotePlugin.setToggleableConditionShouldDisplay(false);

        jira().gotoLoginPage().loginAsSysadminAndGoToHome();
        final JiraVersionTabPage versionTabPage = jira().goTo(JiraVersionTabPage.class, PROJECT_KEY, versionId, ConnectPluginInfo.getPluginKey(), addonAndModuleKey(PLUGIN_KEY,MODULE_KEY));

        assertThat("The addon tab SHOULD NOT be present", versionTabPage.isAddOnTabPresent(), is(false));
    }
}
