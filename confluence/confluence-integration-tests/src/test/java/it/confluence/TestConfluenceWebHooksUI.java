package it.confluence;

import com.atlassian.confluence.pageobjects.page.SearchResultPage;
import com.atlassian.pageobjects.elements.query.Poller;
import com.atlassian.plugin.connect.test.common.servlet.WebHookTestServlet;
import com.atlassian.plugin.connect.test.common.util.TestUser;
import com.atlassian.plugin.connect.test.common.webhook.WebHookBody;
import com.atlassian.plugin.connect.test.common.webhook.WebHookTester;
import com.atlassian.plugin.connect.test.common.webhook.WebHookWaiter;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class TestConfluenceWebHooksUI extends ConfluenceWebDriverTestBase
{
    private static final String SEARCH_TERMS = "Welcome to Confluence";

    private SearchResultPage searchResultPage;

    private TestUser user;

    @Before
    public void setupSearchPage() throws Exception
    {
        user = testUserFactory.basicUser();
        searchResultPage = loginAndVisit(user, SearchResultPage.class);
    }

    private void search(String terms) throws Exception
    {
        searchResultPage.doResultsSearch(terms);
        Poller.waitUntilTrue(searchResultPage.hasMatchingResults());
    }

    @Test
    @Ignore
    public void testSearchPerformedWebHookFired() throws Exception
    {
        WebHookTestServlet.runInJsonRunner(product.getProductInstance().getBaseUrl(), "search_performed", waiter -> {
            search(SEARCH_TERMS);

            final WebHookBody body = waiter.waitForHook();
            assertNotNull(body);
            assertEquals(SEARCH_TERMS, body.find("query"));
            assertEquals(user.getUsername(), body.find("user"));
            assertEquals("conf_all", body.find("spaceCategories[0]"));
        });
    }
}
