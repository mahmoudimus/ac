package it.confluence;

import com.atlassian.confluence.pageobjects.ConfluenceTestedProduct;
import com.atlassian.connect.test.confluence.pageobjects.ConfluenceOps;
import com.atlassian.fugue.Option;
import com.atlassian.plugin.connect.test.product.ConfluenceTestedProductAccessor;
import com.atlassian.plugin.connect.test.common.servlet.WebHookTestServlet;
import com.atlassian.plugin.connect.test.common.webhook.WebHookBody;
import com.atlassian.plugin.connect.test.common.webhook.WebHookTester;
import com.atlassian.plugin.connect.test.common.webhook.WebHookWaiter;
import com.atlassian.plugin.connect.test.confluence.util.ConfluenceTestUserFactory;

import org.junit.Assert;
import org.junit.Test;

public class TestConfluenceWebHooks2
{
    protected static final ConfluenceTestedProduct product = new ConfluenceTestedProductAccessor().getConfluenceProduct();

    private final String baseUrl = new ConfluenceTestedProductAccessor().getConfluenceProduct().getProductInstance().getBaseUrl();

    private final ConfluenceTestUserFactory testUserFactory;

    private ConfluenceOps confluenceOps;

    public TestConfluenceWebHooks2()
    {
        confluenceOps = new ConfluenceOps(baseUrl);
        testUserFactory = new ConfluenceTestUserFactory(product);
    }

    @Test
    public void testSearchPerformedWebHookFired() throws Exception
    {
        WebHookTestServlet.runInJsonRunner(baseUrl, "search_performed", new WebHookTester()
        {
            @Override
            public void test(WebHookWaiter waiter) throws Exception
            {
                final String testQuery = "test";
                String results = String.valueOf(
                        confluenceOps.search(Option.some(testUserFactory.basicUser()), testQuery));
                final WebHookBody body = waiter.waitForHook();
                Assert.assertNotNull(body);
                Assert.assertEquals(testQuery, body.find("query"));
                Assert.assertEquals(results, body.find("results"));
            }
        });
    }

    @Test
    public void testPageCreatedWebHookFired() throws Exception
    {
        WebHookTestServlet.runInJsonRunner(baseUrl, "page_created", new WebHookTester() {
            @Override
            public void test(WebHookWaiter waiter) throws Exception {
                String content = "<h1>Love me</h1>";
                ConfluenceOps.ConfluencePageData pageData = confluenceOps.setPage(Option.some(testUserFactory.basicUser()), "ds", "testWebhook", content);
                final WebHookBody body = waiter.waitForHook();
                Assert.assertNotNull(body);
                Assert.assertEquals(pageData.getId(), body.find("page/id"));
                Assert.assertEquals(pageData.getCreator(), body.find("page/creatorName"));
            }
        });
    }
}
