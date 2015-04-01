package it.confluence;

import com.atlassian.confluence.pageobjects.page.SearchResultPage;
import com.atlassian.pageobjects.elements.query.Poller;
import com.atlassian.plugin.connect.test.webhook.WebHookBody;
import com.atlassian.plugin.connect.test.webhook.WebHookTester;
import com.atlassian.plugin.connect.test.webhook.WebHookWaiter;

import it.util.ConnectTestUserFactory;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import it.util.TestUser;

import static com.atlassian.plugin.connect.test.webhook.WebHookTestServlet.runInJsonRunner;
import static org.junit.Assert.assertNotNull;

public class TestConfluenceWebHooksUI extends ConfluenceWebDriverTestBase
{
    public static final String SEARCH_TERMS = "connect";

    private SearchResultPage searchResultPage;

    @Before
    public void setupSearchPage() throws Exception
    {
        searchResultPage = loginAndVisit(ConnectTestUserFactory.sysadmin(product), SearchResultPage.class);
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
            }
        });
    }

    @Test
    public void testSearchPerformedQueryString() throws Exception
    {
        runInJsonRunner(product.getProductInstance().getBaseUrl(), "search_performed", new WebHookTester()
        {
            @Override
            public void test(WebHookWaiter waiter) throws Exception
            {
                search(SEARCH_TERMS);
                final WebHookBody body = waiter.waitForHook();
                Assert.assertEquals(SEARCH_TERMS, body.find("query"));
            }
        });
    }

    @Test
    public void testSearchPerformedResultsCount() throws Exception
    {
        runInJsonRunner(product.getProductInstance().getBaseUrl(), "search_performed", new WebHookTester()
        {
            @Override
            public void test(WebHookWaiter waiter) throws Exception
            {
                search(SEARCH_TERMS);
                final WebHookBody body = waiter.waitForHook();
                Assert.assertEquals(searchResultPage.getMatchingResults(), Integer.parseInt(body.find("results")));
            }
        });
    }

    @Test
    public void testSearchPerformedUser() throws Exception
    {
        runInJsonRunner(product.getProductInstance().getBaseUrl(), "search_performed", new WebHookTester()
        {
            @Override
            public void test(WebHookWaiter waiter) throws Exception
            {
                search(SEARCH_TERMS);
                final WebHookBody body = waiter.waitForHook();
                Assert.assertEquals("admin", body.find("user"));
            }
        });
    }

    @Test
    public void testSearchPerformedSpaceCategory() throws Exception
    {
        runInJsonRunner(product.getProductInstance().getBaseUrl(), "search_performed", new WebHookTester()
        {
            @Override
            public void test(WebHookWaiter waiter) throws Exception
            {
                search(SEARCH_TERMS);
                final WebHookBody body = waiter.waitForHook();
                Assert.assertEquals("conf_all", body.find("spaceCategories[0]"));
            }
        });
    }
}
