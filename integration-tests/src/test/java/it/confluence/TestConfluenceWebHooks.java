package it.confluence;

import com.atlassian.plugin.remotable.test.pageobjects.confluence.ConfluenceOps;
import com.atlassian.plugin.remotable.test.pageobjects.confluence.FixedConfluenceTestedProduct;
import com.atlassian.plugin.remotable.test.webhook.WebHookBody;
import com.atlassian.plugin.remotable.test.webhook.WebHookTester;
import com.atlassian.plugin.remotable.test.webhook.WebHookWaiter;
import it.AbstractBrowserlessTest;

import org.junit.Assert;
import org.junit.Test;

import java.util.Map;

import static com.atlassian.plugin.remotable.test.webhook.WebHookTestServlet.runInRunner;
import static org.junit.Assert.*;

public class TestConfluenceWebHooks extends AbstractBrowserlessTest
{
    private final ConfluenceOps confluenceOps;

    public TestConfluenceWebHooks()
    {
        super(FixedConfluenceTestedProduct.class);
        confluenceOps = new ConfluenceOps(baseUrl);
    }

    @Test
    public void testSearchPerformedWebHookFired() throws Exception
    {
        runInRunner(baseUrl, "search_performed", new WebHookTester()
        {
            @Override
            public void test(WebHookWaiter waiter) throws Exception
            {
                final String testQuery = "test";
                String results = String.valueOf(
                        confluenceOps.search(testQuery));
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
        runInRunner(baseUrl, "page_created", new WebHookTester()
        {
            @Override
            public void test(WebHookWaiter waiter) throws Exception
            {
                String content = "<h1>Love me</h1>";
                Map pageData = confluenceOps.setPage("ds", "test", content);
                final WebHookBody body = waiter.waitForHook();
                assertNotNull(body);
                Assert.assertEquals(pageData.get("id"), body.find("page/id"));
                Assert.assertEquals(pageData.get("creator"), body.find("page/creatorName"));
            }
        });
	}
}
