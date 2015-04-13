package it.confluence;

import com.atlassian.confluence.pageobjects.ConfluenceTestedProduct;
import com.atlassian.plugin.connect.test.pageobjects.TestedProductProvider;
import com.atlassian.plugin.connect.test.pageobjects.confluence.ConfluenceOps;
import com.atlassian.plugin.connect.test.webhook.WebHookBody;
import com.atlassian.plugin.connect.test.webhook.WebHookTester;
import com.atlassian.plugin.connect.test.webhook.WebHookWaiter;

import it.util.ConnectTestUserFactory;
import org.junit.Assert;
import org.junit.Test;

import it.util.TestUser;

import static com.atlassian.fugue.Option.some;
import static com.atlassian.plugin.connect.test.pageobjects.TestedProductProvider.getConfluenceTestedProduct;
import static com.atlassian.plugin.connect.test.webhook.WebHookTestServlet.runInJsonRunner;
import static org.junit.Assert.assertNotNull;

public class TestConfluenceWebHooks2
{
    protected static final ConfluenceTestedProduct product = TestedProductProvider.getConfluenceTestedProduct();
    private final String baseUrl = getConfluenceTestedProduct().getProductInstance().getBaseUrl();
    private ConfluenceOps confluenceOps;

    public TestConfluenceWebHooks2()
    {
        confluenceOps = new ConfluenceOps(baseUrl);
    }

    @Test
    public void testSearchPerformedWebHookFired() throws Exception
    {
        runInJsonRunner(baseUrl, "search_performed", new WebHookTester()
        {
            @Override
            public void test(WebHookWaiter waiter) throws Exception
            {
                final String testQuery = "test";
                String results = String.valueOf(
                        confluenceOps.search(some(ConnectTestUserFactory.basicUser(product)), testQuery));
                final WebHookBody body = waiter.waitForHook();
                assertNotNull(body);
                Assert.assertEquals(testQuery, body.find("query"));
                Assert.assertEquals(results, body.find("results"));
            }
        });
    }

    @Test
    public void testPageCreatedWebHookFired() throws Exception
    {
        runInJsonRunner(baseUrl, "page_created", new WebHookTester()
        {
            @Override
            public void test(WebHookWaiter waiter) throws Exception
            {
                String content = "<h1>Love me</h1>";
                ConfluenceOps.ConfluencePageData pageData = confluenceOps.setPage(some(ConnectTestUserFactory.basicUser(product)), "ds", "testWebhook", content);
                final WebHookBody body = waiter.waitForHook();
                assertNotNull(body);
                Assert.assertEquals(pageData.getId(), body.find("page/id"));
                Assert.assertEquals(pageData.getCreator(), body.find("page/creatorName"));
            }
        });
    }
}
