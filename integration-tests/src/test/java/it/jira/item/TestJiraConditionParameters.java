package it.jira.item;

import com.atlassian.jira.rest.api.issue.IssueCreateResponse;
import com.atlassian.plugin.connect.modules.beans.AddOnUrlContext;
import com.atlassian.plugin.connect.modules.beans.nested.I18nProperty;
import com.atlassian.plugin.connect.plugin.HttpHeaderNames;
import com.atlassian.plugin.connect.test.AddonTestUtils;
import com.atlassian.plugin.connect.test.pageobjects.RemoteWebItem;
import com.atlassian.plugin.connect.test.pageobjects.jira.JiraViewIssuePage;
import com.atlassian.plugin.connect.test.server.ConnectRunner;
import com.google.common.base.Optional;
import it.jira.JiraWebDriverTestBase;
import it.servlet.condition.ParameterCapturingConditionServlet;
import it.util.TestUser;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.Map;

import static com.atlassian.plugin.connect.modules.beans.WebItemModuleBean.newWebItemBean;
import static com.atlassian.plugin.connect.modules.beans.nested.SingleConditionBean.newSingleConditionBean;
import static com.atlassian.plugin.connect.modules.util.ModuleKeyUtils.addonAndModuleKey;
import static it.matcher.ParamMatchers.isLocale;
import static it.matcher.ParamMatchers.isTimeZone;
import static it.matcher.ParamMatchers.isVersionNumber;
import static it.servlet.condition.ParameterCapturingConditionServlet.PARAMETER_CAPTURE_URL;
import static org.hamcrest.collection.IsMapContaining.hasEntry;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;

public class TestJiraConditionParameters extends JiraWebDriverTestBase
{

    private static ConnectRunner runner;

    private static final String CONTEXT_PARAMETERIZED_WEBITEM = "context-parameterized";

    private static final ParameterCapturingConditionServlet PARAMETER_CAPTURING_SERVLET = new ParameterCapturingConditionServlet();

    @BeforeClass
    public static void startConnectAddOn() throws Exception
    {
        runner = new ConnectRunner(product.getProductInstance().getBaseUrl(), AddonTestUtils.randomAddOnKey())
                .setAuthenticationToNone()
                .addModules("webItems",
                        newWebItemBean()
                                .withName(new I18nProperty("Context Parameterized", CONTEXT_PARAMETERIZED_WEBITEM))
                                .withKey(CONTEXT_PARAMETERIZED_WEBITEM)
                                .withContext(AddOnUrlContext.addon)
                                .withLocation("operations-operations") // issue operations
                                .withWeight(1)
                                .withUrl("/somewhere")
                                .withConditions(
                                        newSingleConditionBean().withCondition(PARAMETER_CAPTURE_URL +
                                                "?issueId={issue.id}&projectKey={project.key}").build()
                                )
                                .build())
                .addRoute(PARAMETER_CAPTURE_URL, PARAMETER_CAPTURING_SERVLET)
                .start();
    }

    @AfterClass
    public static void stopConnectAddOn() throws Exception
    {
        if (runner != null)
        {
            runner.stopAndUninstall();
        }
    }

    @After
    public void tearDown()
    {
        PARAMETER_CAPTURING_SERVLET.clearParams();
    }

    private IssueCreateResponse navigateToJiraIssuePageAndVerifyParameterCapturingWebItem(TestUser user) throws Exception
    {
        IssueCreateResponse issue = product.backdoor().issues().createIssue(project.getKey(), "Nought but a test.");
        JiraViewIssuePage viewIssuePage = loginAndVisit(user, JiraViewIssuePage.class, issue.key);
        String moduleKey = addonAndModuleKey(runner.getAddon().getKey(), CONTEXT_PARAMETERIZED_WEBITEM);
        RemoteWebItem webItem = viewIssuePage.findWebItem(moduleKey, Optional.<String>absent());
        assertNotNull("Web item should be found", webItem);

        return issue;
    }

    @Test
    public void standardParametersArePassedToConditions() throws Exception
    {
        TestUser user = testUserFactory.basicUser();
        navigateToJiraIssuePageAndVerifyParameterCapturingWebItem(user);

        Map<String, String> conditionParams = PARAMETER_CAPTURING_SERVLET.getParamsFromLastRequest();

        assertThat(conditionParams, hasEntry(equalTo("lic"), equalTo("none")));
        assertThat(conditionParams, hasEntry(equalTo("cp"), equalTo("/jira")));
        assertThat(conditionParams, hasEntry(equalTo("tz"), isTimeZone()));
        assertThat(conditionParams, hasEntry(equalTo("loc"), isLocale()));
        assertThat(conditionParams, hasEntry(equalTo("user_id"), equalTo(user.getDisplayName())));
    }

    @Test
    public void contextParametersArePassedToConditions() throws Exception
    {
        TestUser user = testUserFactory.basicUser();
        IssueCreateResponse issue = navigateToJiraIssuePageAndVerifyParameterCapturingWebItem(user);

        Map<String, String> conditionParams = PARAMETER_CAPTURING_SERVLET.getParamsFromLastRequest();

        assertThat(conditionParams, hasEntry(equalTo("issueId"), equalTo(issue.id)));
        assertThat(conditionParams, hasEntry(equalTo("projectKey"), equalTo(project.getKey())));
    }

    @Test
    public void versionNumberIsIncluded() throws Exception
    {
        TestUser user = testUserFactory.basicUser();
        navigateToJiraIssuePageAndVerifyParameterCapturingWebItem(user);

        String version = PARAMETER_CAPTURING_SERVLET.getHttpHeaderFromLastRequest(HttpHeaderNames.ATLASSIAN_CONNECT_VERSION).get();

        assertThat(version, isVersionNumber());
    }
}
