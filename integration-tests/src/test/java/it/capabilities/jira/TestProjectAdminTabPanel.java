package it.capabilities.jira;

import com.atlassian.jira.pageobjects.project.ProjectConfigTabs;
import com.atlassian.jira.pageobjects.project.summary.ProjectSummaryPageTab;
import com.atlassian.plugin.connect.plugin.capabilities.beans.nested.I18nProperty;
import com.atlassian.plugin.connect.test.junit.HtmlDumpRule;
import com.atlassian.plugin.connect.test.pageobjects.jira.AbstractRemotablePluginProjectTab;
import com.atlassian.plugin.connect.test.pageobjects.jira.JiraProjectAdministrationTab;
import com.atlassian.plugin.connect.test.server.ConnectCapabilitiesRunner;
import it.jira.JiraWebDriverTestBase;
import org.hamcrest.Description;
import org.hamcrest.TypeSafeMatcher;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.matchers.JUnitMatchers;

import static com.atlassian.plugin.connect.plugin.capabilities.beans.ConnectProjectAdminTabPanelCapabilityBean.newProjectAdminTabPanelBean;
import static com.atlassian.plugin.connect.plugin.capabilities.beans.RemoteContainerCapabilityBean.newRemoteContainerBean;
import static com.atlassian.plugin.connect.plugin.capabilities.beans.nested.OAuthBean.newOAuthBean;
import static com.atlassian.plugin.connect.test.server.ConnectCapabilitiesRunner.newMustacheServlet;
import static junit.framework.Assert.assertNotNull;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

/**
 * Test of project admin tabs in JIRA.
 */
@Ignore // TODO: skipping until ACDEV-496 resolved. Currently no servlet (IFramePageServlet) is responding causing the test to timeout and fail
public class TestProjectAdminTabPanel extends JiraWebDriverTestBase
{
    private static final String REMOTE_PROJECT_CONFIG_TAB_NAME = "My Connect Project Config";
    private static ConnectCapabilitiesRunner remotePlugin;

    @Rule
    public HtmlDumpRule htmlDump = new HtmlDumpRule(product.getTester().getDriver());

    @BeforeClass
    public static void startConnectAddOn() throws Exception
    {
        remotePlugin = new ConnectCapabilitiesRunner(product.getProductInstance().getBaseUrl(), "my-plugin")
                .addCapability(newProjectAdminTabPanelBean()
                        .withName(new I18nProperty(REMOTE_PROJECT_CONFIG_TAB_NAME, null))
                        .withUrl("/pct")
                        .withWeight(10)
                        .withLocation("projectgroup4")
                        .build())
                .addCapability(newRemoteContainerBean()
                        .withDisplayUrl("http://www.example.com")
//                        .withOAuth(newOAuthBean().withPublicKey("S0m3Publ1cK3y").build())
                        .build())
                .addRoute("/pct", newMustacheServlet("iframe.mu"))
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
                System.out.println(tab.getName());
                System.out.println(tab.getName().equals(REMOTE_PROJECT_CONFIG_TAB_NAME));
                System.out.println(tab.getId());
                return tab.getName().equals(REMOTE_PROJECT_CONFIG_TAB_NAME);
            }

            @Override
            public void describeTo(final Description description)
            {
                description.appendText("Project Configuration Tabs should contain " + REMOTE_PROJECT_CONFIG_TAB_NAME + " tab");
            }
        }));

        final JiraProjectAdministrationTab remoteProjectAdministrationTab =
                page.getTabs().gotoTab("my-connect-project-config",
                        JiraProjectAdministrationTab.class,
                        project.getKey());

        // Test of workaround for JRA-26407.
        assertNotNull(remoteProjectAdministrationTab.getProjectHeader());
        assertEquals(REMOTE_PROJECT_CONFIG_TAB_NAME, remoteProjectAdministrationTab.getTabs().getSelectedTab().getName());
        assertEquals(project.getKey(), remoteProjectAdministrationTab.getProjectKey());
        assertEquals("Success", remoteProjectAdministrationTab.getMessage());
    }

//    @Test
//    public void testProjectTab() throws Exception
//    {
//        testLoggedInAndAnonymous(new Callable()
//        {
//            @Override
//            public Object call() throws Exception
//            {
//                RemotePluginEmbeddedTestPage page = product.visit(BrowseProjectPage.class, project.getKey())
//                                                           .openTab(AppProjectTabPage.class)
//                                                           .getEmbeddedPage();
//
//                assertEquals("Success", page.getMessage());
//                return null;
//            }
//        });
//    }
//
//    public static final class AppProjectTabPage extends AbstractRemotablePluginProjectTab
//    {
//        public AppProjectTabPage(final String projectKey)
//        {
//            super(projectKey, "project-tab-ac-play-project-tab");
//        }
//    }
}
