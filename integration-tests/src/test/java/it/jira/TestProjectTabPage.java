package it.jira;

import com.atlassian.jira.pageobjects.pages.project.BrowseProjectPage;
import com.atlassian.jira.pageobjects.project.ProjectConfigTabs;
import com.atlassian.jira.pageobjects.project.summary.ProjectSummaryPageTab;
import com.atlassian.plugin.connect.test.junit.HtmlDumpRule;
import com.atlassian.plugin.connect.test.pageobjects.RemotePluginEmbeddedTestPage;
import com.atlassian.plugin.connect.test.pageobjects.jira.AbstractRemotablePluginProjectTab;
import com.atlassian.plugin.connect.test.pageobjects.jira.JiraProjectAdministrationTab;
import com.atlassian.plugin.connect.test.server.AtlassianConnectAddOnRunner;
import com.atlassian.plugin.connect.test.server.module.ProjectConfigTabModule;
import com.atlassian.plugin.connect.test.server.module.ProjectTabPageModule;
import it.servlet.ConnectAppServlets;
import org.hamcrest.Description;
import org.hamcrest.TypeSafeMatcher;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.matchers.JUnitMatchers;

import java.util.concurrent.Callable;

import static org.junit.Assert.*;

/**
 * Test of project tabs in JIRA.
 */
public class TestProjectTabPage extends JiraWebDriverTestBase
{
    private static AtlassianConnectAddOnRunner remotePlugin;

    @Rule
    public HtmlDumpRule htmlDump = new HtmlDumpRule(product.getTester().getDriver());

    private static final String REMOTE_PROJECT_CONFIG_TAB_NAME = "Remotable Project Config";

    @BeforeClass
    public static void startConnectAddOn() throws Exception
    {
        remotePlugin = new AtlassianConnectAddOnRunner(product.getProductInstance().getBaseUrl())
                .addOAuth()
                .add(ProjectTabPageModule.key("jira-remotePluginProjectTab")
                         .name("AC Play Project Tab")
                         .path("/ptp")
                         .resource(ConnectAppServlets.apRequestServlet()))
                .add(ProjectConfigTabModule.key(JiraProjectAdministrationTab.MODULE_KEY)
                         .name("Remotable Project Config")
                         .path("/pct")
                         .weight("10")
                         .location("projectgroup3")
                         .resource(ConnectAppServlets.apRequestServlet()))
                .start();
    }

    @AfterClass
    public static void stopConnectAddOn() throws Exception
    {
        if (remotePlugin != null)
        {
            remotePlugin.stop();
        }
    }

    @Test
    public void testViewProjectAdminTab() throws Exception
    {
        loginAsAdmin();
        final ProjectSummaryPageTab page =
                product.visit(ProjectSummaryPageTab.class, project.getKey());

        assertThat(page.getTabs().getTabs(), JUnitMatchers.<ProjectConfigTabs.Tab>hasItem(new TypeSafeMatcher<ProjectConfigTabs.Tab>()
        {

            @Override
            public boolean matchesSafely(final ProjectConfigTabs.Tab tab)
            {
                return tab.getName().equals(REMOTE_PROJECT_CONFIG_TAB_NAME);
            }

            @Override
            public void describeTo(final Description description)
            {
                description.appendText("Project Configuration Tabs should contain Remotable Project Config tab");
            }
        }));

        final JiraProjectAdministrationTab remoteProjectAdministrationTab =
                page.getTabs().gotoTab(
                        JiraProjectAdministrationTab.MODULE_KEY,
                        JiraProjectAdministrationTab.class,
                        project.getKey());

        // Test of workaround for JRA-26407.
        assertNotNull(remoteProjectAdministrationTab.getProjectHeader());
        assertEquals(REMOTE_PROJECT_CONFIG_TAB_NAME, remoteProjectAdministrationTab.getTabs().getSelectedTab().getName());
        assertEquals(project.getKey(), remoteProjectAdministrationTab.getProjectKey());
        assertEquals("Success", remoteProjectAdministrationTab.getMessage());
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
            super(projectKey, "project-tab-jira-remotePluginProjectTab");
        }
    }
}
