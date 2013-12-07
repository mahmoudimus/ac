package it.capabilities.confluence;

import com.atlassian.pageobjects.elements.query.Poller;
import com.atlassian.plugin.connect.test.pageobjects.confluence.FixedConfluenceSearchResultPage;
import com.atlassian.plugin.connect.test.webhook.WebHookBody;
import com.atlassian.plugin.connect.test.webhook.WebHookTester;
import com.atlassian.plugin.connect.test.webhook.WebHookWaiter;
import it.confluence.ConfluenceWebDriverTestBase;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import static com.atlassian.plugin.connect.test.webhook.WebHookTestServlet.runInJsonRunner;
import static org.junit.Assert.assertNotNull;

public class TestConfluenceWebHooksUI extends ConfluenceWebDriverTestBase
{
    public static final String SEARCH_TERMS = "connect";

    private FixedConfluenceSearchResultPage searchResultPage;

    @Before
    public void setupSearchPage() throws Exception
    {
        loginAsAdmin();

        searchResultPage = product.visit(FixedConfluenceSearchResultPage.class);
        searchResultPage.setSearchField(SEARCH_TERMS);
    }

    private void clickSearchButton() throws Exception
    {
        searchResultPage.clickSearchButton();
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
                clickSearchButton();
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
                clickSearchButton();
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
                clickSearchButton();
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
                clickSearchButton();
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
                clickSearchButton();
                final WebHookBody body = waiter.waitForHook();
                Assert.assertEquals("conf_all", body.find("spaceCategories[0]"));
            }
        });
    }
}
