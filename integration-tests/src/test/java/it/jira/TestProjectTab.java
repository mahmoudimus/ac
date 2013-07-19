package it.jira;

import com.atlassian.jira.pageobjects.pages.project.BrowseProjectPage;
import com.atlassian.jira.pageobjects.project.ProjectConfigTabs;
import com.atlassian.jira.pageobjects.project.summary.ProjectSummaryPageTab;
import com.atlassian.plugin.remotable.junit.HtmlDumpRule;
import com.atlassian.plugin.remotable.test.RemotePluginEmbeddedTestPage;
import com.atlassian.plugin.remotable.test.jira.AbstractRemotablePluginProjectTab;
import com.atlassian.plugin.remotable.test.jira.JiraProjectAdministrationTab;
import org.hamcrest.Description;
import org.hamcrest.TypeSafeMatcher;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.openqa.selenium.By;

import java.util.concurrent.Callable;

import static org.hamcrest.Matchers.startsWith;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.junit.matchers.JUnitMatchers.hasItem;

/**
 * Test of project tabs in JIRA.
 */
public class TestProjectTab extends JiraWebDriverTestBase
{
    @Rule
    public HtmlDumpRule htmlDump = new HtmlDumpRule(product.getTester().getDriver());

    private static final String REMOTE_PROJECT_CONFIG_TAB_NAME = "Remotable Project Config";

    @Test
    public void testViewProjectAdminTab() throws Exception
    {
        loginAsAdmin();
        final ProjectSummaryPageTab page =
                product.visit(ProjectSummaryPageTab.class, project.getKey());

        assertThat(page.getTabs().getTabs(), hasItem(new TypeSafeMatcher<ProjectConfigTabs.Tab>()
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
                        "webitem-jira-remotePluginProjectConfigTab",
                        JiraProjectAdministrationTab.class,
                        project.getKey());

        // Test of workaround for JRA-26407.
        assertNotNull(remoteProjectAdministrationTab.getProjectHeader());
        assertEquals(REMOTE_PROJECT_CONFIG_TAB_NAME, remoteProjectAdministrationTab.getTabs().getSelectedTab().getName());
        Assert.assertEquals(project.getKey(), remoteProjectAdministrationTab.getProjectKey());
        Assert.assertEquals("Success", remoteProjectAdministrationTab.getMessage());
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

                Assert.assertEquals("Success", page.getMessage());
                return null;
            }
        });
    }

    @Test
    public void testIFrameIsNotPointingToLocalhost() throws Exception
    {
        loginAsAdmin();
        final String baseJiraUrl = product.getProductInstance().getBaseUrl();
        final RemotePluginEmbeddedTestPage page = product.visit(BrowseProjectPage.class, project.getKey())
                .openTab(AppProjectTabPage.class)
                .getEmbeddedPage();
        final String iFrameSrc = page.getContainerDiv().findElement(By.tagName("iframe")).getAttribute("src");
        assertThat(iFrameSrc, startsWith(baseJiraUrl));
    }

    @Test
    public void testAnonymouslyProjectTabWithRestClient() throws Exception
    {
        logout();
        RemotePluginEmbeddedTestPage page = product.visit(BrowseProjectPage.class, project.getKey())
                .openTab(RestClientProjectTabPage.class)
                .getEmbeddedPage();
        Assert.assertEquals("Success", page.getValue("rest-call-status"));
    }

    public static final class AppProjectTabPage extends AbstractRemotablePluginProjectTab
    {

        public AppProjectTabPage(final String projectKey)
        {
            super(projectKey, "project-tab-jira-remotePluginProjectTab");
        }
    }

    public static final class RestClientProjectTabPage extends AbstractRemotablePluginProjectTab
    {

        public RestClientProjectTabPage(final String projectKey)
        {
            super(projectKey, "project-tab-jira-restClientProjectTab");
        }
    }
}
