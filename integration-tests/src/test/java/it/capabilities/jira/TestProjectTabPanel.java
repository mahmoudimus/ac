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
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.concurrent.Callable;

import static com.atlassian.plugin.connect.modules.beans.ConnectTabPanelModuleBean.newTabPanelBean;
import static org.junit.Assert.assertEquals;

/**
 * Test of project tabs in JIRA.
 */
public class TestProjectTabPanel extends JiraWebDriverTestBase
{
    private static final String PLUGIN_KEY = "my-plugin";
    private static final String MODULE_KEY = "ac-play-project-tab";

    private static ConnectRunner remotePlugin;

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

    public static final class AppProjectTabPage extends AbstractRemotablePluginProjectTab
    {
        public AppProjectTabPage(final String projectKey)
        {
            super(projectKey, PLUGIN_KEY, MODULE_KEY); // my-plugin:ac-play-project-tab-panel
        }
    }
}
