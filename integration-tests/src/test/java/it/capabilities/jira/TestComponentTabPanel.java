package it.capabilities.jira;

import com.atlassian.jira.functest.framework.FunctTestConstants;
import com.atlassian.jira.testkit.client.restclient.Component;
import com.atlassian.jira.testkit.client.restclient.ComponentClient;
import com.atlassian.jira.tests.TestBase;
import com.atlassian.plugin.connect.modules.beans.nested.I18nProperty;
import com.atlassian.plugin.connect.modules.util.ModuleKeyUtils;
import com.atlassian.plugin.connect.plugin.ConnectPluginInfo;
import com.atlassian.plugin.connect.plugin.capabilities.provider.ConnectTabPanelModuleProvider;
import com.atlassian.plugin.connect.test.RemotePluginUtils;
import com.atlassian.plugin.connect.test.pageobjects.jira.JiraComponentTabPage;
import com.atlassian.plugin.connect.test.server.ConnectRunner;
import it.servlet.ConnectAppServlets;
import it.servlet.condition.ParameterCapturingConditionServlet;
import org.junit.*;
import org.junit.rules.TestRule;

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
 * Test of remote component tab panel in JIRA
 */
public class TestComponentTabPanel extends TestBase
{
    private static final String PLUGIN_KEY = RemotePluginUtils.randomPluginKey();
    private static final String MODULE_KEY = "component-tab-panel";

    private static final String PROJECT_KEY = FunctTestConstants.PROJECT_HOMOSAP_KEY;
    private static final String COMPONENT_NAME = "test-component";
    private static ConnectRunner remotePlugin;

    private String componentId;
    private long projectId;

    @Rule
    public TestRule resetToggleableCondition = remotePlugin.resetToggleableConditionRule();

    private static final ParameterCapturingConditionServlet PARAMETER_CAPTURING_SERVLET = new ParameterCapturingConditionServlet();

    @BeforeClass
    public static void setUpClassTest() throws Exception
    {
        remotePlugin = new ConnectRunner(jira().getProductInstance().getBaseUrl(), PLUGIN_KEY)
                .setAuthenticationToNone()
                .addModule(ConnectTabPanelModuleProvider.COMPONENT_TAB_PANELS, newTabPanelBean()
                        .withName(new I18nProperty("Component Tab Panel", null))
                        .withKey(MODULE_KEY)
                        .withUrl("/ipp?component_id={component.id}&project_id={project.id}&project_key={project.key}")
                        .withConditions(
                            toggleableConditionBean(),
                            newSingleConditionBean().withCondition(PARAMETER_CAPTURE_URL +
                                    "?component_id={component.id}&project_id={project.id}&project_key={project.key}").build()
                        )
                        .withWeight(1234)
                        .build())
                .addRoute("/ipp", ConnectAppServlets.apRequestServlet())
                .addRoute(PARAMETER_CAPTURE_URL, PARAMETER_CAPTURING_SERVLET)
                .start();
    }

    @AfterClass
    public static void tearDownClassTest() throws Exception
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
        final ComponentClient componentClient = new ComponentClient(jira().environmentData());
        componentId = Long.toString(componentClient.create(new Component().name(COMPONENT_NAME + System.currentTimeMillis()).project(PROJECT_KEY)).id);
    }

    @After
    public void cleanUpTest()
    {
        backdoor().project().deleteProject(PROJECT_KEY);
    }


    @Test
    public void testComponentTabPanel() throws Exception
    {
        jira().gotoLoginPage().loginAsSysadminAndGoToHome();
        final JiraComponentTabPage componentTabPage = jira().goTo(JiraComponentTabPage.class, PROJECT_KEY, componentId, ConnectPluginInfo.getPluginKey(), addonAndModuleKey(PLUGIN_KEY,MODULE_KEY));

        assertThat("The addon tab should be present", componentTabPage.isAddOnTabPresent(), is(true));

        componentTabPage.clickTab();

        assertThat(componentTabPage.getComponentId(), equalTo(componentId));
        assertThat(componentTabPage.getProjectKey(), equalTo(PROJECT_KEY));

        Map<String,String> conditionRequestParams = PARAMETER_CAPTURING_SERVLET.getParamsFromLastRequest();
        assertThat(conditionRequestParams, hasEntry("component_id", componentId));
        assertThat(conditionRequestParams, hasEntry("project_id", String.valueOf(projectId)));
        assertThat(conditionRequestParams, hasEntry("project_key", PROJECT_KEY));
    }

    @Test
    public void tabIsNotAccessibleWithFalseCondition() throws Exception
    {
        remotePlugin.setToggleableConditionShouldDisplay(false);

        jira().gotoLoginPage().loginAsSysadminAndGoToHome();
        final JiraComponentTabPage componentTabPage = jira().goTo(JiraComponentTabPage.class, PROJECT_KEY, componentId, ConnectPluginInfo.getPluginKey(), addonAndModuleKey(PLUGIN_KEY,MODULE_KEY));

        assertThat("The addon tab SHOULD NOT be present", componentTabPage.isAddOnTabPresent(), is(false));
    }

}
