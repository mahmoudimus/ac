package it.jira.item;

import com.atlassian.plugin.connect.modules.beans.AddOnUrlContext;
import com.atlassian.plugin.connect.modules.beans.nested.CompositeConditionType;
import com.atlassian.plugin.connect.modules.beans.nested.I18nProperty;
import com.atlassian.plugin.connect.plugin.HttpHeaderNames;
import com.atlassian.plugin.connect.test.AddonTestUtils;
import com.atlassian.plugin.connect.test.pageobjects.RemoteWebItem;
import com.atlassian.plugin.connect.test.pageobjects.jira.JiraViewIssuePage;
import com.atlassian.plugin.connect.test.pageobjects.jira.JiraViewProjectPage;
import com.atlassian.plugin.connect.test.server.ConnectRunner;
import com.google.common.base.Optional;

import hudson.plugins.jira.soap.RemoteIssue;
import it.jira.JiraWebDriverTestBase;
import it.servlet.condition.CheckUsernameConditionServlet;
import it.servlet.condition.ParameterCapturingConditionServlet;
import it.util.ConnectTestUserFactory;
import it.util.TestUser;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.Map;

import static com.atlassian.plugin.connect.modules.beans.WebItemModuleBean.newWebItemBean;
import static com.atlassian.plugin.connect.modules.beans.nested.CompositeConditionBean.newCompositeConditionBean;
import static com.atlassian.plugin.connect.modules.beans.nested.SingleConditionBean.newSingleConditionBean;
import static com.atlassian.plugin.connect.modules.util.ModuleKeyUtils.addonAndModuleKey;
import static it.matcher.ParamMatchers.isLocale;
import static it.matcher.ParamMatchers.isTimeZone;
import static it.matcher.ParamMatchers.isVersionNumber;
import static it.servlet.condition.ParameterCapturingConditionServlet.PARAMETER_CAPTURE_URL;
import static org.hamcrest.collection.IsMapContaining.hasEntry;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.*;

public class TestJiraConditions extends JiraWebDriverTestBase
{
    private static ConnectRunner runner;

    private static String onlyBettyWebItem;
    private static String bettyAndBarneyWebitem;
    private static final String ADMIN_RIGHTS_WEBITEM = "admin-rights";
    private static final String CONTEXT_PARAMETERIZED_WEBITEM = "context-parameterized";

    private static String onlyBettyConditionUrl;
    private static String onlyBarneyConditionUrl;

    private static final ParameterCapturingConditionServlet PARAMETER_CAPTURING_SERVLET = new ParameterCapturingConditionServlet();

    private static TestUser betty;
    private static TestUser barney;

    @BeforeClass
    public static void startConnectAddOn() throws Exception
    {
        betty = testUserFactory.admin();
        barney = testUserFactory.basicUser();

        onlyBettyWebItem = "only-" + betty.getDisplayName();
        bettyAndBarneyWebitem = betty.getDisplayName() + "-and-" + barney.getDisplayName();
        onlyBettyConditionUrl = "/only" + betty.getDisplayName() + "Condition";
        onlyBarneyConditionUrl = "/only" + barney.getDisplayName() + "Condition";

        runner = new ConnectRunner(product.getProductInstance().getBaseUrl(), AddonTestUtils.randomAddOnKey())
                .setAuthenticationToNone()
                .addModules("webItems",
                    newWebItemBean()
                        .withName(new I18nProperty("Only Betty", onlyBettyWebItem))
                        .withKey(onlyBettyWebItem)
                        .withLocation("system.top.navigation.bar")
                        .withWeight(1)
                        .withUrl("http://www.google.com")
                        .withConditions(
                                newSingleConditionBean().withCondition("user_is_logged_in").build(),
                                newSingleConditionBean().withCondition(onlyBettyConditionUrl).build()
                        )
                        .build(),
                    newWebItemBean()
                        .withName(new I18nProperty("Betty And Barney", bettyAndBarneyWebitem))
                        .withKey(bettyAndBarneyWebitem)
                        .withLocation("system.top.navigation.bar")
                        .withWeight(1)
                        .withUrl("http://www.google.com")
                        .withConditions(
                                newSingleConditionBean().withCondition("user_is_logged_in").build(),
                                newCompositeConditionBean()
                                    .withType(CompositeConditionType.OR)
                                    .withConditions(
                                            newSingleConditionBean().withCondition(onlyBettyConditionUrl).build(),
                                            newSingleConditionBean().withCondition(onlyBarneyConditionUrl).build()
                                    ).build()
                        )
                        .build(),
                    newWebItemBean()
                        .withName(new I18nProperty("Admin Rights", ADMIN_RIGHTS_WEBITEM))
                        .withKey(ADMIN_RIGHTS_WEBITEM)
                        .withLocation("system.top.navigation.bar")
                        .withWeight(1)
                        .withUrl("http://www.google.com")
                        .withConditions(
                            newSingleConditionBean().withCondition("user_is_admin").build()
                        )
                        .build(),
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
                .addRoute(onlyBarneyConditionUrl, new CheckUsernameConditionServlet(barney))
                .addRoute(onlyBettyConditionUrl, new CheckUsernameConditionServlet(betty))
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

    @Test
    public void bettyCanSeeBettyWebItem()
    {
        JiraViewProjectPage viewProjectPage = loginAndVisit(betty, JiraViewProjectPage.class, project.getKey());
        RemoteWebItem webItem = viewProjectPage.findWebItem(getModuleKey(onlyBettyWebItem), Optional.<String>absent());
        assertNotNull("Web item should be found", webItem);
    }

    @Test
    public void barneyCannotSeeBettyWebItem()
    {
        JiraViewProjectPage viewProjectPage = loginAndVisit(barney, JiraViewProjectPage.class, project.getKey());
        assertTrue("Web item should NOT be found", viewProjectPage.webItemDoesNotExist(getModuleKey(onlyBettyWebItem)));
    }
    
    @Test
    public void adminCannotSeeBettyWebItem()
    {
        TestUser admin = testUserFactory.admin();

        JiraViewProjectPage viewProjectPage = loginAndVisit(admin, JiraViewProjectPage.class, project.getKey());
        assertTrue("Web item should NOT be found", viewProjectPage.webItemDoesNotExist(getModuleKey(onlyBettyWebItem)));
    }

    @Test
    public void bettyCanSeeBettyAndBarneyWebItem()
    {
        JiraViewProjectPage viewProjectPage = loginAndVisit(betty, JiraViewProjectPage.class, project.getKey());
        RemoteWebItem webItem = viewProjectPage.findWebItem(getModuleKey(bettyAndBarneyWebitem), Optional.<String>absent());
        assertNotNull("Web item should be found", webItem);
    }

    @Test
    public void barneyCanSeeBettyAndBarneyWebItem()
    {
        JiraViewProjectPage viewProjectPage = loginAndVisit(barney, JiraViewProjectPage.class, project.getKey());
        RemoteWebItem webItem = viewProjectPage.findWebItem(getModuleKey(bettyAndBarneyWebitem), Optional.<String>absent());
        assertNotNull("Web item should be found", webItem);
    }

    @Test
    public void adminCannotSeeBettyAndBarneyWebItem()
    {
        TestUser admin = testUserFactory.admin();

        JiraViewProjectPage viewProjectPage = loginAndVisit(admin, JiraViewProjectPage.class, project.getKey());
        assertTrue("Web item should NOT be found", viewProjectPage.webItemDoesNotExist(getModuleKey(bettyAndBarneyWebitem)));
    }

    @Test
    public void bettyCanSeeAdminRightsWebItem()
    {
        JiraViewProjectPage viewProjectPage = loginAndVisit(betty, JiraViewProjectPage.class, project.getKey());
        RemoteWebItem webItem = viewProjectPage.findWebItem(getModuleKey(ADMIN_RIGHTS_WEBITEM), Optional.<String>absent());
        assertNotNull("Web item should be found", webItem);
    }

    @Test
    public void barneyCannotSeeAdminRightsWebItem()
    {
        JiraViewProjectPage viewProjectPage = loginAndVisit(barney, JiraViewProjectPage.class, project.getKey());
        assertTrue("Web item should NOT be found", viewProjectPage.webItemDoesNotExist(getModuleKey(ADMIN_RIGHTS_WEBITEM)));
    }

    @Test
    public void adminCanSeeAdminRightsWebItem()
    {
        TestUser admin = testUserFactory.admin();

        JiraViewProjectPage viewProjectPage = loginAndVisit(admin, JiraViewProjectPage.class, project.getKey());
        RemoteWebItem webItem = viewProjectPage.findWebItem(getModuleKey(ADMIN_RIGHTS_WEBITEM), Optional.<String>absent());
        assertNotNull("Web item should be found", webItem);
    }

    private RemoteIssue navigateToJiraIssuePageAndVerifyParameterCapturingWebItem(TestUser user) throws Exception
    {
        login(user);

        RemoteIssue issue = jiraOps.createIssue(project.getKey(), "Nought but a test.");
        JiraViewIssuePage viewIssuePage = product.visit(JiraViewIssuePage.class, issue.getKey());
        RemoteWebItem webItem = viewIssuePage.findWebItem(getModuleKey(CONTEXT_PARAMETERIZED_WEBITEM), Optional.<String>absent());
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
        RemoteIssue issue = navigateToJiraIssuePageAndVerifyParameterCapturingWebItem(user);

        Map<String, String> conditionParams = PARAMETER_CAPTURING_SERVLET.getParamsFromLastRequest();

        assertThat(conditionParams, hasEntry(equalTo("issueId"), equalTo(issue.getId())));
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

    private String getModuleKey(String module)
    {
        return addonAndModuleKey(runner.getAddon().getKey(), module);
    }    

}
