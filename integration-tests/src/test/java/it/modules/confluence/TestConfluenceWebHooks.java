package it.modules.confluence;

import com.atlassian.plugin.connect.test.pageobjects.confluence.ConfluenceOps;
import com.atlassian.plugin.connect.test.pageobjects.confluence.FixedConfluenceTestedProduct;
import com.atlassian.plugin.connect.test.webhook.WebHookBody;
import com.atlassian.plugin.connect.test.webhook.WebHookTester;
import com.atlassian.plugin.connect.test.webhook.WebHookWaiter;
import it.AbstractBrowserlessTest;
import org.junit.Assert;
import org.junit.Test;

import static com.atlassian.fugue.Option.some;
import static com.atlassian.plugin.connect.test.webhook.WebHookTestServlet.runInJsonRunner;
import static it.TestConstants.ADMIN_USERNAME;
import static org.junit.Assert.assertNotNull;

public class TestConfluenceWebHooks extends AbstractBrowserlessTest
{
    private ConfluenceOps confluenceOps;
    private ConfluenceOps.ConfluenceUser admin;

    public TestConfluenceWebHooks()
    {
        super(FixedConfluenceTestedProduct.class);
        confluenceOps = new ConfluenceOps(baseUrl);
        admin = new ConfluenceOps.ConfluenceUser(ADMIN_USERNAME, ADMIN_USERNAME);
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
                        confluenceOps.search(some(admin), testQuery));
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
                ConfluenceOps.ConfluencePageData pageData = confluenceOps.setPage(some(admin), "ds", "testWebhook", content);
                final WebHookBody body = waiter.waitForHook();
                assertNotNull(body);
                Assert.assertEquals(pageData.getId(), body.find("page/id"));
                Assert.assertEquals(pageData.getCreator(), body.find("page/creatorName"));
            }
        });
    }
}
