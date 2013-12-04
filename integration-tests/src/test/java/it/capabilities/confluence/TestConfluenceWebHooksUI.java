package it.capabilities.confluence;

import com.atlassian.pageobjects.elements.query.Poller;
import com.atlassian.plugin.connect.test.pageobjects.confluence.FixedConfluenceSearchResultPage;
import com.atlassian.plugin.connect.test.webhook.WebHookBody;
import com.atlassian.plugin.connect.test.webhook.WebHookTester;
import com.atlassian.plugin.connect.test.webhook.WebHookWaiter;
import it.confluence.ConfluenceWebDriverTestBase;
import org.junit.Assert;
import org.junit.Test;

import static com.atlassian.plugin.connect.test.webhook.WebHookTestServlet.runInJsonRunner;
import static org.junit.Assert.assertNotNull;

public class TestConfluenceWebHooksUI extends ConfluenceWebDriverTestBase
{
    public static final String SEARCH_TERMS = "connect";

    @Test
    public void testSearchPerformedWebHookFired() throws Exception
    {
        runInJsonRunner(product.getProductInstance().getBaseUrl(), "search_performed", new WebHookTester()
        {
            @Override
            public void test(WebHookWaiter waiter) throws Exception
            {
                loginAsAdmin();

                FixedConfluenceSearchResultPage searchResultPage = product.visit(FixedConfluenceSearchResultPage.class);
                searchResultPage.setSearchField(SEARCH_TERMS);
                searchResultPage.clickSearchButton();
                Poller.waitUntilTrue(searchResultPage.hasMatchingResults());

                int matchingResults = searchResultPage.getMatchingResults();

                final WebHookBody body = waiter.waitForHook();
                assertNotNull(body);
                Assert.assertEquals(SEARCH_TERMS, body.find("query"));
                Assert.assertEquals(matchingResults, Integer.parseInt(body.find("results")));
                Assert.assertEquals("admin", body.find("user"));
                Assert.assertEquals("conf_all", body.find("spaceCategories[0]"));
            }
        });
    }
}
