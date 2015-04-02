package it.jira.iframe;

import com.atlassian.jira.pageobjects.project.ProjectConfigTabs;
import com.atlassian.jira.pageobjects.project.summary.ProjectSummaryPageTab;
import com.atlassian.plugin.connect.modules.beans.nested.I18nProperty;
import com.atlassian.plugin.connect.plugin.capabilities.provider.ConnectProjectAdminTabPanelModuleProvider;
import com.atlassian.plugin.connect.test.AddonTestUtils;
import com.atlassian.plugin.connect.test.helptips.JiraHelpTipApiClient;
import com.atlassian.plugin.connect.test.pageobjects.jira.JiraProjectAdministrationTab;
import com.atlassian.plugin.connect.test.server.ConnectRunner;
import it.jira.JiraWebDriverTestBase;
import it.servlet.ConnectAppServlets;
import it.servlet.condition.ParameterCapturingConditionServlet;
import it.util.TestUser;
import org.hamcrest.Description;
import org.hamcrest.TypeSafeMatcher;
import org.hamcrest.core.IsCollectionContaining;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestRule;

import java.rmi.RemoteException;
import java.util.Map;

import static com.atlassian.plugin.connect.modules.beans.ConnectProjectAdminTabPanelModuleBean.newProjectAdminTabPanelBean;
import static com.atlassian.plugin.connect.modules.beans.nested.SingleConditionBean.newSingleConditionBean;
import static com.atlassian.plugin.connect.modules.util.ModuleKeyUtils.addonAndModuleKey;
import static it.servlet.condition.ParameterCapturingConditionServlet.PARAMETER_CAPTURE_URL;
import static it.servlet.condition.ToggleableConditionServlet.toggleableConditionBean;
import static junit.framework.Assert.assertNotNull;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.Matchers.hasEntry;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

/**
 * Test of project admin tabs in JIRA.
 */
public class TestProjectAdminTabPanel extends JiraWebDriverTestBase
{
    private static final String PROJECT_CONFIG_MODULE_KEY = "my-connect-project-config";
    private static final String PROJECT_CONFIG_TAB_NAME = "My Connect Project Config";

    private static ConnectRunner remotePlugin;

    private static final ParameterCapturingConditionServlet PARAMETER_CAPTURING_SERVLET = new ParameterCapturingConditionServlet();

    @Rule
    public TestRule resetToggleableCondition = remotePlugin.resetToggleableConditionRule();

    @BeforeClass
    public static void startConnectAddOn() throws Exception
    {
        remotePlugin = new ConnectRunner(product.getProductInstance().getBaseUrl(), AddonTestUtils.randomAddOnKey())
                .setAuthenticationToNone()
                .addModule(ConnectProjectAdminTabPanelModuleProvider.PROJECT_ADMIN_TAB_PANELS, newProjectAdminTabPanelBean()
                        .withName(new I18nProperty(PROJECT_CONFIG_TAB_NAME, null))
                        .withKey(PROJECT_CONFIG_MODULE_KEY)
                        .withUrl("/pct")
                        .withWeight(10)
                        .withLocation("projectgroup4")
                        .withConditions(
                            toggleableConditionBean(),
                            newSingleConditionBean().withCondition(PARAMETER_CAPTURE_URL +
                                    "?projectKey={project.key}&projectId={project.id}").build()
                        )
                        .build())
                .addRoute("/pct", ConnectAppServlets.apRequestServlet())
                .addRoute(PARAMETER_CAPTURE_URL, PARAMETER_CAPTURING_SERVLET)
                .start();

        new JiraHelpTipApiClient(product, TestUser.ADMIN).dismissConfigureProjectTips();
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
        final ProjectSummaryPageTab page = loginAndVisit(TestUser.ADMIN, ProjectSummaryPageTab.class, project.getKey());

        assertThat(page.getTabs().getTabs(), IsCollectionContaining.<ProjectConfigTabs.Tab>hasItem(projectConfigTabMatcher(PROJECT_CONFIG_TAB_NAME)));

        final String linkId = addonAndModuleKey(remotePlugin.getAddon().getKey(), PROJECT_CONFIG_MODULE_KEY);
        final JiraProjectAdministrationTab remoteProjectAdministrationTab =
                page.getTabs().gotoTab(linkId, JiraProjectAdministrationTab.class, project.getKey(), remotePlugin.getAddon().getKey(), PROJECT_CONFIG_MODULE_KEY);

        // Test of workaround for JRA-26407.
        assertNotNull(remoteProjectAdministrationTab.getProjectHeader());

        assertEquals(PROJECT_CONFIG_TAB_NAME, remoteProjectAdministrationTab.getTabs().getSelectedTab().getName());
        assertEquals(project.getKey(), remoteProjectAdministrationTab.getProjectKey());
        assertEquals("Success", remoteProjectAdministrationTab.getMessage());

        Map<String,String> conditionRequestParams = PARAMETER_CAPTURING_SERVLET.getParamsFromLastRequest();
        assertThat(conditionRequestParams, hasEntry("projectKey", project.getKey()));
        assertThat(conditionRequestParams, hasEntry("projectId", project.getId()));
    }

    @Test
    public void tabIsNotAccessibleWithFalseCondition() throws RemoteException
    {
        ProjectSummaryPageTab page = loginAndVisit(TestUser.ADMIN, ProjectSummaryPageTab.class, project.getKey());
        assertThat("AddOn project config tab should be present", page.getTabs().getTabs(),
                IsCollectionContaining.<ProjectConfigTabs.Tab>hasItem(projectConfigTabMatcher(PROJECT_CONFIG_TAB_NAME)));

        remotePlugin.setToggleableConditionShouldDisplay(false);

        page = product.visit(ProjectSummaryPageTab.class, project.getKey());
        assertThat("AddOn project config tab should NOT be present", page.getTabs().getTabs(),
                not(IsCollectionContaining.<ProjectConfigTabs.Tab>hasItem(projectConfigTabMatcher(PROJECT_CONFIG_TAB_NAME))));
    }

    private TypeSafeMatcher<ProjectConfigTabs.Tab> projectConfigTabMatcher(final String tabName)
    {
        return new TypeSafeMatcher<ProjectConfigTabs.Tab>()
        {

            @Override
            public boolean matchesSafely(final ProjectConfigTabs.Tab tab)
            {
                return tab.getName().equals(tabName);
            }

            @Override
            public void describeTo(final Description description)
            {
                description.appendText("Project Configuration Tabs should contain " + tabName + " tab");
            }
        };
    }
}
