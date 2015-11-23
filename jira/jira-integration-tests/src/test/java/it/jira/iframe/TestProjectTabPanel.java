package it.jira.iframe;

import com.atlassian.connect.test.jira.pageobjects.JiraProjectSummaryPageWithAddonTab;
import com.atlassian.plugin.connect.modules.beans.nested.I18nProperty;
import com.atlassian.plugin.connect.test.common.pageobjects.ConnectAddOnEmbeddedTestPage;
import com.atlassian.plugin.connect.test.common.servlet.ConnectAppServlets;
import com.atlassian.plugin.connect.test.common.servlet.ConnectRunner;
import com.atlassian.plugin.connect.test.common.servlet.condition.ParameterCapturingConditionServlet;
import com.atlassian.plugin.connect.test.common.util.AddonTestUtils;
import it.jira.JiraWebDriverTestBase;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestRule;

import java.util.Map;

import static com.atlassian.plugin.connect.modules.beans.ConnectTabPanelModuleBean.newTabPanelBean;
import static com.atlassian.plugin.connect.modules.beans.nested.SingleConditionBean.newSingleConditionBean;
import static com.atlassian.plugin.connect.test.common.servlet.ToggleableConditionServlet.toggleableConditionBean;
import static com.atlassian.plugin.connect.test.common.servlet.condition.ParameterCapturingConditionServlet.PARAMETER_CAPTURE_URL;
import static org.hamcrest.Matchers.hasEntry;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

/**
 * Test of project tabs in JIRA.
 */
public class TestProjectTabPanel extends JiraWebDriverTestBase
{
    private static final String ADDON_KEY = AddonTestUtils.randomAddOnKey();
    private static final String MODULE_KEY = "ac-test-project-tab";
    private static final String MODULE_TITLE = "AC Test Project Tab";

    private static ConnectRunner addon;

    @Rule
    public TestRule resetToggleableCondition = addon.resetToggleableConditionRule();

    private static final ParameterCapturingConditionServlet PARAMETER_CAPTURING_SERVLET = new ParameterCapturingConditionServlet();

    @BeforeClass
    public static void setUpClass() throws Exception
    {
        logout();

        addon = new ConnectRunner(product.getProductInstance().getBaseUrl(), ADDON_KEY)
                .setAuthenticationToNone()
                .addModule("jiraProjectTabPanels", newTabPanelBean()
                        .withName(new I18nProperty(MODULE_TITLE, null))
                        .withKey(MODULE_KEY)
                        .withUrl("/ptp")
                        .withWeight(1234)
                        .withConditions(
                                toggleableConditionBean(),
                                newSingleConditionBean().withCondition(PARAMETER_CAPTURE_URL +
                                        "?issueId={issue.id}&projectKey={project.key}&projectId={project.id}").build()
                        )
                        .build()
                )
                .addRoute("/ptp", ConnectAppServlets.apRequestServlet())
                .addRoute(PARAMETER_CAPTURE_URL, PARAMETER_CAPTURING_SERVLET)
                .start();
    }

    @AfterClass
    public static void stopConnectAddOn() throws Exception
    {
        if (addon != null)
        {
            addon.stopAndUninstall();
        }
    }

    @Test
    public void projectTabShouldBePresentAndReceiveContextParameters() throws Exception
    {
        login(testUserFactory.basicUser());
        JiraProjectSummaryPageWithAddonTab summaryPage = product.visit(
                JiraProjectSummaryPageWithAddonTab.class, project.getKey(), ADDON_KEY, MODULE_KEY);
        summaryPage = summaryPage.expandAddonsList();
        ConnectAddOnEmbeddedTestPage embeddedAddonTestPage = summaryPage.goToEmbeddedTestPageAddon();
        assertEquals("Success", embeddedAddonTestPage.getMessage());

        Map<String, String> conditionRequestParams = PARAMETER_CAPTURING_SERVLET.getParamsFromLastRequest();
        assertThat(conditionRequestParams, hasEntry("projectKey", project.getKey()));
        assertThat(conditionRequestParams, hasEntry("projectId", project.getId()));
    }
}
