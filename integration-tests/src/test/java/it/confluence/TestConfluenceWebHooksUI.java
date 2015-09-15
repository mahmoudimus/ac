package it.confluence;

import com.atlassian.confluence.webdriver.pageobjects.page.SearchResultPage;
import com.atlassian.pageobjects.elements.query.Poller;
import com.atlassian.plugin.connect.test.webhook.WebHookBody;
import com.atlassian.plugin.connect.test.webhook.WebHookTester;
import com.atlassian.plugin.connect.test.webhook.WebHookWaiter;
import it.util.TestUser;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import static com.atlassian.plugin.connect.test.webhook.WebHookTestServlet.runInJsonRunner;
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
    public void testSearchPerformedWebHookFired() throws Exception
    {
        runInJsonRunner(product.getProductInstance().getBaseUrl(), "search_performed", new WebHookTester()
        {
            @Override
            public void test(WebHookWaiter waiter) throws Exception
            {
                search(SEARCH_TERMS);

                final WebHookBody body = waiter.waitForHook();
                assertNotNull(body);
                Assert.assertEquals(SEARCH_TERMS, body.find("query"));
                Assert.assertEquals(user.getUsername(), body.find("user"));
                Assert.assertEquals("conf_all", body.find("spaceCategories[0]"));
            }
        });
    }
}
