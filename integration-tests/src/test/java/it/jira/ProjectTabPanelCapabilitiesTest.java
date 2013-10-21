package it.jira;

import com.atlassian.jira.pageobjects.pages.project.BrowseProjectPage;
import com.atlassian.plugin.connect.plugin.capabilities.beans.nested.I18nProperty;
import com.atlassian.plugin.connect.test.junit.HtmlDumpRule;
import com.atlassian.plugin.connect.test.pageobjects.RemotePluginEmbeddedTestPage;
import com.atlassian.plugin.connect.test.pageobjects.jira.AbstractRemotablePluginProjectTab;
import com.atlassian.plugin.connect.test.server.ConnectCapabilitiesRunner;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;

import java.util.concurrent.Callable;

import static com.atlassian.plugin.connect.plugin.capabilities.beans.ConnectProjectTabPanelCapabilityBean.newProjectTabPanelBean;
import static org.junit.Assert.assertEquals;

/**
 * Test of project tabs in JIRA.
 */
public class ProjectTabPanelCapabilitiesTest extends JiraWebDriverTestBase
{
    private static ConnectCapabilitiesRunner remotePlugin;

    @Rule
    public HtmlDumpRule htmlDump = new HtmlDumpRule(product.getTester().getDriver());

    private static final String REMOTE_PROJECT_CONFIG_TAB_NAME = "Remotable Project Config";

    @BeforeClass
    public static void startConnectAddOn() throws Exception
    {
        remotePlugin = new ConnectCapabilitiesRunner(product.getProductInstance().getBaseUrl(),"my-plugin")
                .addOAuth()
                .addCapability(newProjectTabPanelBean()
                        .withKey("jira-remotePluginProjectTab")
                        .withName(new I18nProperty("AC Play Project Tab", "my.projecttabpanel"))
                        .withUrl("/ptp")
                        .withWeight(1234)
                        .build())
// TODO: TestProjectTabPage had a second module for project config tab. Need to cover this but likely in another class
//                .addCapability(newProjectTabPanelBean()
//                        .withKey("jira-remotePluginProjectConfigTab")
//                        .withName(new I18nProperty("Remotable Project Config", "my.projecttabpanel2"))
//                        .withUrl("/pct")
//                        .withWeight(10)
//                        .build())
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

//    @Test
//    public void testViewProjectAdminTab() throws Exception
//    {
//        loginAsAdmin();
//        final ProjectSummaryPageTab page =
//                product.visit(ProjectSummaryPageTab.class, project.getKey());
//
//        assertThat(page.getTabs().getTabs(), JUnitMatchers.<ProjectConfigTabs.Tab>hasItem(new TypeSafeMatcher<ProjectConfigTabs.Tab>()
//        {
//
//            @Override
//            public boolean matchesSafely(final ProjectConfigTabs.Tab tab)
//            {
//                return tab.getName().equals(REMOTE_PROJECT_CONFIG_TAB_NAME);
//            }
//
//            @Override
//            public void describeTo(final Description description)
//            {
//                description.appendText("Project Configuration Tabs should contain Remotable Project Config tab");
//            }
//        }));
//
//        final JiraProjectAdministrationTab remoteProjectAdministrationTab =
//                page.getTabs().gotoTab(
//                        "jira-remotePluginProjectConfigTab",
//                        JiraProjectAdministrationTab.class,
//                        project.getKey());
//
//        // Test of workaround for JRA-26407.
//        assertNotNull(remoteProjectAdministrationTab.getProjectHeader());
//        assertEquals(REMOTE_PROJECT_CONFIG_TAB_NAME, remoteProjectAdministrationTab.getTabs().getSelectedTab().getName());
//        assertEquals(project.getKey(), remoteProjectAdministrationTab.getProjectKey());
//        assertEquals("Success", remoteProjectAdministrationTab.getMessage());
//    }

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
