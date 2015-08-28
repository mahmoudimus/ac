package it.jira.iframe;

import com.atlassian.jira.rest.api.issue.IssueCreateResponse;
import com.atlassian.plugin.connect.jira.capabilities.provider.ConnectTabPanelModuleProvider;
import com.atlassian.plugin.connect.modules.beans.nested.I18nProperty;
import com.atlassian.plugin.connect.modules.beans.nested.ScopeName;
import com.atlassian.plugin.connect.test.AddonTestUtils;
import com.atlassian.plugin.connect.test.pageobjects.jira.JiraViewIssuePage;
import com.atlassian.plugin.connect.test.pageobjects.jira.JiraViewIssuePageWithRemotePluginIssueTab;
import com.atlassian.plugin.connect.test.server.ConnectRunner;
import it.jira.JiraWebDriverTestBase;
import it.servlet.ConnectAppServlets;
import it.servlet.condition.ParameterCapturingConditionServlet;
import it.util.TestProject;
import it.util.TestUser;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestRule;

import java.rmi.RemoteException;
import java.util.Map;

import static com.atlassian.plugin.connect.modules.beans.ConnectTabPanelModuleBean.newTabPanelBean;
import static com.atlassian.plugin.connect.modules.beans.nested.SingleConditionBean.newSingleConditionBean;
import static com.atlassian.plugin.connect.modules.util.ModuleKeyUtils.addonAndModuleKey;
import static it.servlet.condition.ParameterCapturingConditionServlet.PARAMETER_CAPTURE_URL;
import static it.servlet.condition.ToggleableConditionServlet.toggleableConditionBean;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasEntry;
import static org.hamcrest.core.Is.is;

/**
 * Test of remote issue tab panel in JIRA
 */
public class TestIssueTabPanel extends JiraWebDriverTestBase
{
    private static final String PLUGIN_KEY = AddonTestUtils.randomAddOnKey();
    private static final String MODULE_KEY = "issue-tab-panel";
    private static ConnectRunner remotePlugin;

    @Rule
    public TestRule resetToggleableCondition = remotePlugin.resetToggleableConditionRule();

    private static final ParameterCapturingConditionServlet PARAMETER_CAPTURING_SERVLET = new ParameterCapturingConditionServlet();

    private TestUser user;
    private IssueCreateResponse issue;

    @BeforeClass
    public static void startConnectAddOn() throws Exception
    {
        remotePlugin = new ConnectRunner(product.getProductInstance().getBaseUrl(), PLUGIN_KEY)
                .setAuthenticationToNone()
                .addModule(ConnectTabPanelModuleProvider.ISSUE_TAB_PANELS, newTabPanelBean()
                        .withName(new I18nProperty("Issue Tab Panel", null))
                        .withKey(MODULE_KEY)
                        .withUrl("/ipp?issue_id={issue.id}&project_id={project.id}&project_key={project.key}")
                        .withConditions(
                            toggleableConditionBean(),
                            newSingleConditionBean().withCondition(PARAMETER_CAPTURE_URL +
                                    "?issue_id={issue.id}&issue_key={issue.key}&project_id={project.id}&project_key={project.key}&issue_type_id={issuetype.id}").build()
                        )
                        .withWeight(1234)
                        .build())
                .addRoute("/ipp", ConnectAppServlets.apRequestServlet())
                .addRoute(PARAMETER_CAPTURE_URL, PARAMETER_CAPTURING_SERVLET)
                .addScope(ScopeName.READ)
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

    @Before
    public void setUpTest() throws Exception
    {
        user = testUserFactory.basicUser();
        issue = product.backdoor().issues().createIssue(project.getKey(), "Test issue for tab", user.getUsername());
    }

    @Test
    public void testIssueTabPanel() throws RemoteException
    {
        login(testUserFactory.basicUser());
        JiraViewIssuePageWithRemotePluginIssueTab page = product.visit(
                JiraViewIssuePageWithRemotePluginIssueTab.class, "issue-tab-panel", issue.key(), PLUGIN_KEY);
        assertThat(page.getMessage(), is("Success"));

        final String expectedIssueTypeId = product.backdoor().issues().getIssue(issue.id).fields.issuetype.id;

        Map<String,String> conditionRequestParams = PARAMETER_CAPTURING_SERVLET.getParamsFromLastRequest();
        assertThat(conditionRequestParams, hasEntry("issue_id", issue.id()));
        assertThat(conditionRequestParams, hasEntry("issue_key", issue.key()));
        assertThat(conditionRequestParams, hasEntry("issue_type_id", expectedIssueTypeId));
        assertThat(conditionRequestParams, hasEntry("project_id", project.getId()));
        assertThat(conditionRequestParams, hasEntry("project_key", project.getKey()));
    }

    @Test
    public void tabIsNotAccessibleWithFalseCondition() throws RemoteException
    {
        String completeKey = addonAndModuleKey(PLUGIN_KEY,MODULE_KEY);

        login(testUserFactory.basicUser());

        // tab panel should be present
        JiraViewIssuePage page = product.visit(JiraViewIssuePage.class, issue.key());
        assertThat(page.isTabPanelPresent(completeKey), is(true));

        remotePlugin.setToggleableConditionShouldDisplay(false);

        page = product.visit(JiraViewIssuePage.class, issue.key());
        assertThat(page.isTabPanelPresent(completeKey), is(false));
    }

}
