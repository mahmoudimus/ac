package it.capabilities.jira;

import com.atlassian.jira.pageobjects.project.ProjectConfigTabs;
import com.atlassian.jira.pageobjects.project.summary.ProjectSummaryPageTab;
import com.atlassian.plugin.connect.modules.beans.nested.I18nProperty;
import com.atlassian.plugin.connect.plugin.capabilities.provider.ConnectProjectAdminTabPanelModuleProvider;
import com.atlassian.plugin.connect.test.pageobjects.jira.JiraProjectAdministrationTab;
import com.atlassian.plugin.connect.test.server.ConnectRunner;
import it.jira.JiraWebDriverTestBase;
import it.servlet.ConnectAppServlets;
import org.hamcrest.Description;
import org.hamcrest.TypeSafeMatcher;
import org.hamcrest.core.IsCollectionContaining;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import static com.atlassian.plugin.connect.modules.beans.ConnectProjectAdminTabPanelModuleBean.newProjectAdminTabPanelBean;
import static junit.framework.Assert.assertNotNull;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

/**
 * Test of project admin tabs in JIRA.
 */
public class TestProjectAdminTabPanel extends JiraWebDriverTestBase
{
    private static final String PLUGIN_KEY = "my-plugin";
    private static final String PROJECT_CONFIG_MODULE_KEY = "my-connect-project-config";
    private static final String PROJECT_CONFIG_TAB_NAME = "My Connect Project Config";

    private static ConnectRunner remotePlugin;

    @BeforeClass
    public static void startConnectAddOn() throws Exception
    {
        remotePlugin = new ConnectRunner(product.getProductInstance().getBaseUrl(), PLUGIN_KEY)
                .setAuthenticationToNone()
                .addModule(ConnectProjectAdminTabPanelModuleProvider.PROJECT_ADMIN_TAB_PANELS, newProjectAdminTabPanelBean()
                        .withName(new I18nProperty(PROJECT_CONFIG_TAB_NAME, null))
                        .withKey(PROJECT_CONFIG_MODULE_KEY)
                        .withUrl("/pct")
                        .withWeight(10)
                        .withLocation("projectgroup4")
                        .build())
                .addRoute("/pct", ConnectAppServlets.apRequestServlet())
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
                description.appendText("Project Configuration Tabs should contain " + PROJECT_CONFIG_TAB_NAME + " tab");
            }
        }));

        final JiraProjectAdministrationTab remoteProjectAdministrationTab =
                page.getTabs().gotoTab(PROJECT_CONFIG_MODULE_KEY, JiraProjectAdministrationTab.class, project.getKey(), PROJECT_CONFIG_MODULE_KEY);

        // Test of workaround for JRA-26407.
        assertNotNull(remoteProjectAdministrationTab.getProjectHeader());
        assertEquals(PROJECT_CONFIG_TAB_NAME, remoteProjectAdministrationTab.getTabs().getSelectedTab().getName());
        assertEquals(project.getKey(), remoteProjectAdministrationTab.getProjectKey());
        assertEquals("Success", remoteProjectAdministrationTab.getMessage());
    }

}
