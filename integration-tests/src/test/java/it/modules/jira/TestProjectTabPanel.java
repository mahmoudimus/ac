package it.modules.jira;

import com.atlassian.jira.projects.pageobjects.page.BrowseProjectPage;
import com.atlassian.plugin.connect.modules.beans.nested.I18nProperty;
import com.atlassian.plugin.connect.plugin.ConnectPluginInfo;
import com.atlassian.plugin.connect.plugin.capabilities.provider.ConnectTabPanelModuleProvider;
import com.atlassian.plugin.connect.test.AddonTestUtils;
import com.atlassian.plugin.connect.test.pageobjects.RemotePluginEmbeddedTestPage;
import com.atlassian.plugin.connect.test.pageobjects.jira.AbstractRemotablePluginProjectTab;
import com.atlassian.plugin.connect.test.server.ConnectRunner;
import it.jira.JiraWebDriverTestBase;
import it.servlet.ConnectAppServlets;
import it.servlet.condition.ParameterCapturingConditionServlet;
import org.junit.*;
import org.junit.rules.TestRule;

import java.util.Map;
import java.util.concurrent.Callable;

import static com.atlassian.plugin.connect.modules.beans.ConnectTabPanelModuleBean.newTabPanelBean;
import static com.atlassian.plugin.connect.modules.beans.nested.SingleConditionBean.newSingleConditionBean;
import static com.atlassian.plugin.connect.modules.util.ModuleKeyUtils.addonAndModuleKey;
import static it.servlet.condition.ParameterCapturingConditionServlet.PARAMETER_CAPTURE_URL;
import static it.servlet.condition.ToggleableConditionServlet.toggleableConditionBean;
import static org.hamcrest.Matchers.hasEntry;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

/**
 * Test of project tabs in JIRA.
 */
public class TestProjectTabPanel extends JiraWebDriverTestBase
{
    private static final String PLUGIN_KEY = AddonTestUtils.randomAddOnKey();
    private static final String MODULE_KEY = "ac-play-project-tab";

    private static ConnectRunner remotePlugin;

    @Rule
    public TestRule resetToggleableCondition = remotePlugin.resetToggleableConditionRule();

    private static final ParameterCapturingConditionServlet PARAMETER_CAPTURING_SERVLET = new ParameterCapturingConditionServlet();

    @BeforeClass
    public static void startConnectAddOn() throws Exception
    {
        remotePlugin = new ConnectRunner(product.getProductInstance().getBaseUrl(), PLUGIN_KEY)
                .setAuthenticationToNone()
                .addModule(ConnectTabPanelModuleProvider.PROJECT_TAB_PANELS, newTabPanelBean()
                        .withName(new I18nProperty("AC Play Project Tab", null))
                        .withKey(MODULE_KEY)
                        .withUrl("/ptp")
                        .withWeight(1234)
                        .withConditions(
                            toggleableConditionBean(),
                            newSingleConditionBean().withCondition(PARAMETER_CAPTURE_URL +
                                    "?issueId={issue.id}&projectKey={project.key}&projectId={project.id}").build()
                        )
                        .build()
                )
                .addRoute("/ptp", ConnectAppServlets.apRequestServlet())
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

    @Test
    public void testProjectTab() throws Exception
    {
        testLoggedInAndAnonymous(new Callable()
        {
            @Override
            public Object call() throws Exception
            {
                RemotePluginEmbeddedTestPage page = product.visit(BrowseProjectPage.class, project.getKey())
                                                           .openTab(AppProjectTabPage.class)
                                                           .getEmbeddedPage();
                assertEquals("Success", page.getMessage());

                Map<String,String> conditionRequestParams = PARAMETER_CAPTURING_SERVLET.getParamsFromLastRequest();
                assertThat(conditionRequestParams, hasEntry("projectKey", project.getKey()));
                assertThat(conditionRequestParams, hasEntry("projectId", project.getId()));

                return null;
            }
        });
    }

    @Test
    public void tabIsNotAccessibleWithFalseCondition() throws Exception
    {
        loginAsAdmin();
        BrowseProjectPage browseProjectPage = product.visit(BrowseProjectPage.class, project.getKey());
        assertThat("AddOn project tab should be present", browseProjectPage.hasTab(AppProjectTabPage.class), is(true));
        remotePlugin.setToggleableConditionShouldDisplay(false);
        browseProjectPage = product.visit(BrowseProjectPage.class, project.getKey());
        assertThat("AddOn project tab should NOT be present", browseProjectPage.hasTab(AppProjectTabPage.class), is(false));
    }

    public static final class AppProjectTabPage extends AbstractRemotablePluginProjectTab
    {
        private static String projectKey;

        public AppProjectTabPage()
        {
            this(projectKey);
        }

        public AppProjectTabPage(final String projectKey)
        {
            super(projectKey, ConnectPluginInfo.getPluginKey(), addonAndModuleKey(PLUGIN_KEY,MODULE_KEY)); // my-plugin:ac-play-project-tab-panel
        }
    }

    /**
     * this hack means AppProjectTabPage can have a no-arg constructor (which BrowseProjectPage.hasTab() seems to require)
     */
    @Before
    public void setStaticProjectKey()
    {
        AppProjectTabPage.projectKey = project.getKey();
    }

}
