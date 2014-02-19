package it.capabilities.jira;

import com.atlassian.jira.pageobjects.pages.project.BrowseProjectPage;
import com.atlassian.plugin.connect.modules.beans.nested.I18nProperty;
import com.atlassian.plugin.connect.plugin.capabilities.provider.ConnectTabPanelModuleProvider;
import com.atlassian.plugin.connect.test.pageobjects.RemotePluginEmbeddedTestPage;
import com.atlassian.plugin.connect.test.pageobjects.jira.AbstractRemotablePluginProjectTab;
import com.atlassian.plugin.connect.test.server.ConnectRunner;
import it.jira.JiraWebDriverTestBase;
import it.servlet.ConnectAppServlets;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestRule;

import java.util.concurrent.Callable;

import static com.atlassian.plugin.connect.modules.beans.ConnectTabPanelModuleBean.newTabPanelBean;
import static it.servlet.condition.ToggleableConditionServlet.toggleableConditionBean;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

/**
 * Test of project tabs in JIRA.
 */
public class TestProjectTabPanel extends JiraWebDriverTestBase
{
    private static final String PLUGIN_KEY = "my-plugin";
    private static final String MODULE_KEY = "ac-play-project-tab";

    private static ConnectRunner remotePlugin;

    @Rule
    public TestRule resetToggleableCondition = remotePlugin.resetToggleableConditionRule();

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
                        .withConditions(toggleableConditionBean())
                        .build())
                .addRoute("/ptp", ConnectAppServlets.apRequestServlet())
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
            super(projectKey, PLUGIN_KEY, MODULE_KEY); // my-plugin:ac-play-project-tab-panel
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
