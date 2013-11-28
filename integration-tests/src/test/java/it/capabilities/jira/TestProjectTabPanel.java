package it.capabilities.jira;

import com.atlassian.jira.pageobjects.pages.project.BrowseProjectPage;
import com.atlassian.plugin.connect.plugin.capabilities.beans.nested.I18nProperty;
import com.atlassian.plugin.connect.plugin.capabilities.provider.ConnectTabPanelModuleProvider;
import com.atlassian.plugin.connect.test.pageobjects.RemotePluginEmbeddedTestPage;
import com.atlassian.plugin.connect.test.pageobjects.jira.AbstractRemotablePluginProjectTab;
import com.atlassian.plugin.connect.test.server.ConnectCapabilitiesRunner;
import it.jira.JiraWebDriverTestBase;
import it.servlet.ConnectAppServlets;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.concurrent.Callable;

import static com.atlassian.plugin.connect.plugin.capabilities.beans.ConnectTabPanelCapabilityBean.newTabPanelBean;
import static org.junit.Assert.assertEquals;

/**
 * Test of project tabs in JIRA.
 */
public class TestProjectTabPanel extends JiraWebDriverTestBase
{
    private static ConnectCapabilitiesRunner remotePlugin;

    @BeforeClass
    public static void startConnectAddOn() throws Exception
    {
        remotePlugin = new ConnectCapabilitiesRunner(product.getProductInstance().getBaseUrl(), "my-plugin")
                .addCapability(ConnectTabPanelModuleProvider.PROJECT_TAB_PANELS, newTabPanelBean()
                        .withName(new I18nProperty("AC Play Project Tab", null))
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
            super(projectKey, "project-tab-ac-play-project-tab");
        }
    }
}
