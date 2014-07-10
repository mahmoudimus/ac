package it.jira;

import com.atlassian.jira.pageobjects.project.ProjectConfigTabs;
import com.atlassian.jira.pageobjects.project.summary.ProjectSummaryPageTab;
import com.atlassian.jira.projects.pageobjects.page.BrowseProjectPage;
import com.atlassian.plugin.connect.api.xmldescriptor.XmlDescriptor;
import com.atlassian.plugin.connect.plugin.module.jira.projecttab.ProjectTabPageModuleDescriptor;
import com.atlassian.plugin.connect.test.pageobjects.RemotePluginEmbeddedTestPage;
import com.atlassian.plugin.connect.test.pageobjects.jira.AbstractRemotablePluginProjectTab;
import com.atlassian.plugin.connect.test.pageobjects.jira.JiraProjectAdministrationTab;
import com.atlassian.plugin.connect.test.server.AtlassianConnectAddOnRunner;
import com.atlassian.plugin.connect.test.server.module.ProjectConfigTabModule;
import com.atlassian.plugin.connect.test.server.module.ProjectTabPageModule;
import it.servlet.ConnectAppServlets;
import org.hamcrest.Description;
import org.hamcrest.TypeSafeMatcher;
import org.hamcrest.core.IsCollectionContaining;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.concurrent.Callable;

import static org.junit.Assert.*;

/**
 * Test of project tabs in JIRA.
 */
@XmlDescriptor
public class TestProjectTabPage extends JiraWebDriverTestBase
{
    private static final String PROJECT_TAB_MODULE_KEY = "project-tab";
    private static final String ACTUAL_PROJECT_TAB_MODULE_KEY = ProjectTabPageModuleDescriptor.PROJECT_TAB_PAGE_MODULE_PREFIX + PROJECT_TAB_MODULE_KEY;
    private static final String PROJECT_TAB_NAME = "Project Tab";

    private static final String PROJECT_CONFIG_MODULE_KEY = "project-config-tab";
    private static final String PROJECT_CONFIG_TAB_NAME = "Project Config Tab";

    private static AtlassianConnectAddOnRunner remotePlugin;

    @BeforeClass
    public static void startConnectAddOn() throws Exception
    {
        remotePlugin = new AtlassianConnectAddOnRunner(product.getProductInstance().getBaseUrl())
                .addOAuth()
                .add(ProjectTabPageModule.key(PROJECT_TAB_MODULE_KEY)
                         .name(PROJECT_TAB_NAME)
                         .path("/ptp")
                         .resource(ConnectAppServlets.apRequestServlet()))
                .add(ProjectConfigTabModule.key(PROJECT_CONFIG_MODULE_KEY)
                         .name(PROJECT_CONFIG_TAB_NAME)
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
            remotePlugin.stopAndUninstall();
        }
    }

    @Test
    public void testViewProjectAdminTab() throws Exception
    {
        loginAsAdmin();
        final ProjectSummaryPageTab page =
                product.visit(ProjectSummaryPageTab.class, project.getKey());

        assertThat(page.getTabs().getTabs(), IsCollectionContaining.<ProjectConfigTabs.Tab>hasItem(new TypeSafeMatcher<ProjectConfigTabs.Tab>()
        {

            @Override
            public boolean matchesSafely(final ProjectConfigTabs.Tab tab)
            {
                return tab.getName().equals(PROJECT_CONFIG_TAB_NAME);
            }

            @Override
            public void describeTo(final Description description)
            {
                description.appendText("Project Configuration Tabs should contain tab: " + PROJECT_CONFIG_TAB_NAME);
            }
        }));

        final JiraProjectAdministrationTab remoteProjectAdministrationTab = page.getTabs().gotoTab(PROJECT_CONFIG_MODULE_KEY, JiraProjectAdministrationTab.class, project.getKey(), PROJECT_CONFIG_MODULE_KEY, "servlet-");

        // Test of workaround for JRA-26407.
        assertNotNull(remoteProjectAdministrationTab.getProjectHeader());
        assertEquals(PROJECT_CONFIG_TAB_NAME, remoteProjectAdministrationTab.getTabs().getSelectedTab().getName());
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
            super(projectKey, remotePlugin.getPluginKey(), ACTUAL_PROJECT_TAB_MODULE_KEY);
        }
    }
}
