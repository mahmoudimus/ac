package it.capabilities.jira;

import com.atlassian.plugin.connect.modules.beans.SearchRequestViewModuleBean;
import com.atlassian.plugin.connect.modules.beans.nested.I18nProperty;
import com.atlassian.plugin.connect.test.pageobjects.jira.IssueNavigatorViewsMenu;
import com.atlassian.plugin.connect.test.pageobjects.jira.JiraAdvancedSearchPage;
import com.atlassian.plugin.connect.test.server.ConnectRunner;
import com.atlassian.plugin.connect.test.utils.NameValuePairs;
import hudson.plugins.jira.soap.RemoteIssue;
import it.jira.JiraWebDriverTestBase;
import it.servlet.ConnectAppServlets;
import it.servlet.EchoQueryParametersServlet;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import static com.atlassian.plugin.connect.modules.beans.nested.SingleConditionBean.newSingleConditionBean;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.collection.IsCollectionWithSize.hasSize;

public class TestSearchRequestView extends JiraWebDriverTestBase
{
    private static final String LABEL = "A Search Request View";
    private static final String SERVLET_URL = "/search";

    private static ConnectRunner remotePlugin;
    private static EchoQueryParametersServlet searchRequestViewServlet;

    @BeforeClass
    public static void startConnectAddOn() throws Exception
    {
        searchRequestViewServlet = new EchoQueryParametersServlet();

        remotePlugin = new ConnectRunner(product.getProductInstance().getBaseUrl(), "my-plugin")
                .addInstallLifecycle()
                .addModule("jiraSearchRequestViews", SearchRequestViewModuleBean.newSearchRequestViewModuleBean()
                        .withWeight(100)
                        .withUrl(SERVLET_URL)
                        .withName(new I18nProperty(LABEL, null))
                        .withDescription(new I18nProperty("A description", null))
                        .withConditions(
                                newSingleConditionBean().withCondition("user_is_logged_in").build())
                        .build())
                .addRoute(SERVLET_URL, ConnectAppServlets.wrapContextAwareServlet(searchRequestViewServlet))
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
    public void verifyEntryIsPresentWhenLoggedIn() throws Exception
    {
        loginAsAdmin();
        IssueNavigatorViewsMenu.ViewEntry entry = findSearchRequestViewEntry();

        assertThat(entry.isPresent(), is(true));
    }

    @Test
    public void verifyEntryIsNotPresentWhenUnauthenticated() throws Exception
    {
        IssueNavigatorViewsMenu.ViewEntry entry = findSearchRequestViewEntry();

        assertThat(entry.isPresent(), is(false));
    }

    @Test
    public void verifyIssueKeyIsPartOfUrl() throws Exception
    {
        loginAsAdmin();
        RemoteIssue issue = createIssue();
        findSearchRequestViewEntry().click();
        NameValuePairs queryParameters = searchRequestViewServlet.waitForQueryParameters();

        assertNoTimeout(queryParameters);
        assertThat(queryParameters.any("issues").getValue(), containsString(issue.getKey()));
    }

    @Test
    public void verifyPaginationParametersArePartOfUrl() throws Exception
    {
        NameValuePairs queryParameters = logInAndGetSearchRequestViewQueryParameters();
        assertThat(queryParameters.all("startIssue"), hasSize(greaterThan(0)));
        assertThat(queryParameters.all("endIssue"), hasSize(greaterThan(0)));
        assertThat(queryParameters.all("totalIssues"), hasSize(greaterThan(0)));
    }

    @Test
    public void verifyOAuthParametersAreNotPartOfUrl() throws Exception
    {
        NameValuePairs queryParameters = logInAndGetSearchRequestViewQueryParameters();
        assertThat(queryParameters.allStartingWith("oauth_"), hasSize(0));
    }

    @Test
    public void verifyJwtParameterIsPartOfUrl() throws Exception
    {
        NameValuePairs queryParameters = logInAndGetSearchRequestViewQueryParameters();
        assertThat(queryParameters.allStartingWith("jwt"), hasSize(1));
    }

    private NameValuePairs logInAndGetSearchRequestViewQueryParameters() throws Exception
    {
        loginAsAdmin();
        createIssue();
        findSearchRequestViewEntry().click();
        NameValuePairs queryParameters = searchRequestViewServlet.waitForQueryParameters();

        assertNoTimeout(queryParameters);
        return queryParameters;
    }

    private IssueNavigatorViewsMenu.ViewEntry findSearchRequestViewEntry() throws Exception
    {
        JiraAdvancedSearchPage searchPage = product.visit(JiraAdvancedSearchPage.class);
        searchPage.enterQuery("project = " + project.getKey()).submit();
        IssueNavigatorViewsMenu viewsMenu = searchPage.viewsMenu().open();
        return viewsMenu.entryWithLabel(LABEL);
    }

    private RemoteIssue createIssue() throws Exception
    {
        return jiraOps.createIssue(project.getKey(), "test issue");
    }

    private void assertNoTimeout(NameValuePairs queryParameters)
    {
        assertThat("Request did not time out", queryParameters != null);
    }

}
