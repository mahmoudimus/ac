package it.modules.jira.condition;

import com.atlassian.plugin.connect.modules.beans.AddOnUrlContext;
import com.atlassian.plugin.connect.modules.beans.nested.CompositeConditionType;
import com.atlassian.plugin.connect.modules.beans.nested.I18nProperty;
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
import org.junit.After;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.Map;

import static com.atlassian.plugin.connect.modules.beans.WebItemModuleBean.newWebItemBean;
import static com.atlassian.plugin.connect.modules.beans.nested.CompositeConditionBean.newCompositeConditionBean;
import static com.atlassian.plugin.connect.modules.beans.nested.SingleConditionBean.newSingleConditionBean;
import static com.atlassian.plugin.connect.modules.util.ModuleKeyUtils.addonAndModuleKey;
import static it.util.TestConstants.BARNEY_USERNAME;
import static it.util.TestConstants.BETTY_USERNAME;
import static it.matcher.ParamMatchers.isLocale;
import static it.matcher.ParamMatchers.isTimeZone;
import static it.servlet.condition.ParameterCapturingConditionServlet.PARAMETER_CAPTURE_URL;
import static org.hamcrest.collection.IsMapContaining.hasEntry;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.*;

public class TestJiraConditions extends JiraWebDriverTestBase
{
    private static ConnectRunner remotePlugin;

    private static final String ONLY_BETTY_WEBITEM = "only-betty";
    private static final String BETTY_AND_BARNEY_WEBITEM = "betty-and-barney";
    private static final String ADMIN_RIGHTS_WEBITEM = "admin-rights";
    private static final String CONTEXT_PARAMETERIZED_WEBITEM = "context-parameterized";

    private static final String ONLY_BETTY_CONDITION_URL = "/onlyBettyCondition";
    private static final String ONLY_BARNEY_CONDITION_URL = "/onlyBarneyCondition";

    private static final ParameterCapturingConditionServlet PARAMETER_CAPTURING_SERVLET = new ParameterCapturingConditionServlet();

    @BeforeClass
    public static void startConnectAddOn() throws Exception
    {
        remotePlugin = new ConnectRunner(product.getProductInstance().getBaseUrl(), AddonTestUtils.randomAddOnKey())
                .setAuthenticationToNone()
                .addModules("webItems",
                    newWebItemBean()
                        .withName(new I18nProperty("Only Betty", ONLY_BETTY_WEBITEM))
                        .withKey(ONLY_BETTY_WEBITEM)
                        .withLocation("system.top.navigation.bar")
                        .withWeight(1)
                        .withUrl("http://www.google.com")
                        .withConditions(
                                newSingleConditionBean().withCondition("user_is_logged_in").build(),
                                newSingleConditionBean().withCondition(ONLY_BETTY_CONDITION_URL).build()
                        )
                        .build(),
                    newWebItemBean()
                        .withName(new I18nProperty("Betty And Barney", BETTY_AND_BARNEY_WEBITEM))
                        .withKey(BETTY_AND_BARNEY_WEBITEM)
                        .withLocation("system.top.navigation.bar")
                        .withWeight(1)
                        .withUrl("http://www.google.com")
                        .withConditions(
                                newSingleConditionBean().withCondition("user_is_logged_in").build(),
                                newCompositeConditionBean()
                                    .withType(CompositeConditionType.OR)
                                    .withConditions(
                                            newSingleConditionBean().withCondition(ONLY_BETTY_CONDITION_URL).build(),
                                            newSingleConditionBean().withCondition(ONLY_BARNEY_CONDITION_URL).build()
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
                .addRoute(ONLY_BARNEY_CONDITION_URL, new CheckUsernameConditionServlet(BARNEY_USERNAME))
                .addRoute(ONLY_BETTY_CONDITION_URL, new CheckUsernameConditionServlet(BETTY_USERNAME))
                .addRoute(PARAMETER_CAPTURE_URL, PARAMETER_CAPTURING_SERVLET)
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

    @After
    public void tearDown()
    {
        PARAMETER_CAPTURING_SERVLET.clearParams();
    }

    @Test
    public void bettyCanSeeBettyWebItem()
    {
        loginAsBetty();

        JiraViewProjectPage viewProjectPage = product.visit(JiraViewProjectPage.class, project.getKey());
        RemoteWebItem webItem = viewProjectPage.findWebItem(getModuleKey(ONLY_BETTY_WEBITEM), Optional.<String>absent());
        assertNotNull("Web item should be found", webItem);
    }

    @Test
    public void barneyCannotSeeBettyWebItem()
    {
        loginAsBarney();

        JiraViewProjectPage viewProjectPage = product.visit(JiraViewProjectPage.class, project.getKey());
        assertTrue("Web item should NOT be found", viewProjectPage.webItemDoesNotExist(getModuleKey(ONLY_BETTY_WEBITEM)));
    }
    
    @Test
    public void adminCannotSeeBettyWebItem()
    {
        loginAsAdmin();

        JiraViewProjectPage viewProjectPage = product.visit(JiraViewProjectPage.class, project.getKey());
        assertTrue("Web item should NOT be found", viewProjectPage.webItemDoesNotExist(getModuleKey(ONLY_BETTY_WEBITEM)));
    }

    @Test
    public void bettyCanSeeBettyAndBarneyWebItem()
    {
        loginAsBetty();

        JiraViewProjectPage viewProjectPage = product.visit(JiraViewProjectPage.class, project.getKey());
        RemoteWebItem webItem = viewProjectPage.findWebItem(getModuleKey(BETTY_AND_BARNEY_WEBITEM), Optional.<String>absent());
        assertNotNull("Web item should be found", webItem);
    }

    @Test
    public void barneyCanSeeBettyAndBarneyWebItem()
    {
        loginAsBarney();

        JiraViewProjectPage viewProjectPage = product.visit(JiraViewProjectPage.class, project.getKey());
        RemoteWebItem webItem = viewProjectPage.findWebItem(getModuleKey(BETTY_AND_BARNEY_WEBITEM), Optional.<String>absent());
        assertNotNull("Web item should be found", webItem);
    }

    @Test
    public void adminCannotSeeBettyAndBarneyWebItem()
    {
        loginAsAdmin();

        JiraViewProjectPage viewProjectPage = product.visit(JiraViewProjectPage.class, project.getKey());
        assertTrue("Web item should NOT be found", viewProjectPage.webItemDoesNotExist(getModuleKey(BETTY_AND_BARNEY_WEBITEM)));
    }

    @Test
    public void bettyCanSeeAdminRightsWebItem()
    {
        loginAsBetty();

        JiraViewProjectPage viewProjectPage = product.visit(JiraViewProjectPage.class, project.getKey());
        RemoteWebItem webItem = viewProjectPage.findWebItem(getModuleKey(ADMIN_RIGHTS_WEBITEM), Optional.<String>absent());
        assertNotNull("Web item should be found", webItem);
    }

    @Test
    public void barneyCannotSeeAdminRightsWebItem()
    {
        loginAsBarney();

        JiraViewProjectPage viewProjectPage = product.visit(JiraViewProjectPage.class, project.getKey());
        assertTrue("Web item should NOT be found", viewProjectPage.webItemDoesNotExist(getModuleKey(ADMIN_RIGHTS_WEBITEM)));
    }

    @Test
    public void adminCanSeeAdminRightsWebItem()
    {
        loginAsAdmin();

        JiraViewProjectPage viewProjectPage = product.visit(JiraViewProjectPage.class, project.getKey());
        RemoteWebItem webItem = viewProjectPage.findWebItem(getModuleKey(ADMIN_RIGHTS_WEBITEM), Optional.<String>absent());
        assertNotNull("Web item should be found", webItem);
    }

    private RemoteIssue navigateToJiraIssuePageAndVerifyParameterCapturingWebItem() throws Exception
    {
        loginAsAdmin();

        RemoteIssue issue = jiraOps.createIssue(project.getKey(), "Nought but a test.");
        JiraViewIssuePage viewIssuePage = product.visit(JiraViewIssuePage.class, issue.getKey());
        RemoteWebItem webItem = viewIssuePage.findWebItem(getModuleKey(CONTEXT_PARAMETERIZED_WEBITEM), Optional.<String>absent());
        assertNotNull("Web item should be found", webItem);

        return issue;
    }

    @Test
    public void standardParametersArePassedToConditions() throws Exception
    {
        navigateToJiraIssuePageAndVerifyParameterCapturingWebItem();

        Map<String, String> conditionParams = PARAMETER_CAPTURING_SERVLET.getParamsFromLastRequest();

        assertThat(conditionParams, hasEntry(equalTo("lic"), equalTo("none")));
        assertThat(conditionParams, hasEntry(equalTo("cp"), equalTo("/jira")));
        assertThat(conditionParams, hasEntry(equalTo("tz"), isTimeZone()));
        assertThat(conditionParams, hasEntry(equalTo("loc"), isLocale()));
        assertThat(conditionParams, hasEntry(equalTo("user_id"), equalTo("admin")));
    }

    @Test
    public void contextParametersArePassedToConditions() throws Exception
    {
        RemoteIssue issue = navigateToJiraIssuePageAndVerifyParameterCapturingWebItem();

        Map<String, String> conditionParams = PARAMETER_CAPTURING_SERVLET.getParamsFromLastRequest();

        assertThat(conditionParams, hasEntry(equalTo("issueId"), equalTo(issue.getId())));
        assertThat(conditionParams, hasEntry(equalTo("projectKey"), equalTo(project.getKey())));
    }
    
    private String getModuleKey(String module)
    {
        return addonAndModuleKey(remotePlugin.getAddon().getKey(),module);
    }    

}
