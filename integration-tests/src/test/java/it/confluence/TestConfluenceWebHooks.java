package it.confluence;

import com.atlassian.plugin.connect.plugin.capabilities.ConvertToWiredTest;
import com.atlassian.plugin.connect.test.AddonTestUtils;
import com.atlassian.plugin.connect.test.pageobjects.confluence.ConfluenceOps;
import com.atlassian.plugin.connect.test.pageobjects.confluence.FixedConfluenceTestedProduct;
import com.atlassian.plugin.connect.test.webhook.WebHookBody;
import com.atlassian.plugin.connect.test.webhook.WebHookTester;
import com.atlassian.plugin.connect.test.webhook.WebHookWaiter;

import it.AbstractBrowserlessTest;
import it.util.TestUser;

import org.junit.Assert;
import org.junit.Test;

import static com.atlassian.fugue.Option.some;
import static com.atlassian.plugin.connect.test.webhook.WebHookTestServlet.runInRunner;
import static it.matcher.ParamMatchers.isVersionNumber;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;

@ConvertToWiredTest
public class TestConfluenceWebHooks extends AbstractBrowserlessTest
{
    private ConfluenceOps confluenceOps;

    public TestConfluenceWebHooks()
    {
        super(FixedConfluenceTestedProduct.class);
        confluenceOps = new ConfluenceOps(baseUrl);
    }

    @Test
    public void testSearchPerformedWebHookFired() throws Exception
    {
        final String pluginKey = AddonTestUtils.randomAddOnKey();
        
        runInRunner(baseUrl, "search_performed", pluginKey, new WebHookTester()
        {
            @Override
            public void test(WebHookWaiter waiter) throws Exception
            {
                final String testQuery = "test";
                String results = String.valueOf(
                        confluenceOps.search(some(TestUser.ADMIN), testQuery));
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
        final String pluginKey = AddonTestUtils.randomAddOnKey();
        
        runInRunner(baseUrl, "page_created", pluginKey, new WebHookTester()
        {
            @Override
            public void test(WebHookWaiter waiter) throws Exception
            {
                String content = "<h1>Love me</h1>";
                ConfluenceOps.ConfluencePageData pageData = confluenceOps.setPage(some(TestUser.ADMIN), "ds", "testxmlWebhooks", content);
                final WebHookBody body = waiter.waitForHook();
                assertNotNull(body);
                Assert.assertEquals(pageData.getId(), body.find("page/id"));
                Assert.assertEquals(pageData.getCreator(), body.find("page/creatorName"));
            }
        });
    }

    @Test
    public void testVersionIsIncluded() throws Exception
    {
        final String pluginKey = AddonTestUtils.randomAddOnKey();

        runInRunner(baseUrl, "page_created", pluginKey, new WebHookTester()
        {
            @Override
            public void test(WebHookWaiter waiter) throws Exception
            {
                String content = "<h1>I'm a test page</h1>";
                confluenceOps.setPage(some(TestUser.ADMIN), "ds", "testxmlWebhooks", content);
                final WebHookBody body = waiter.waitForHook();
                assertNotNull(body);
                assertThat(body.getVersion(),isVersionNumber());
            }
        });
    }

    @Test
    public void testContentPermissionsUpdatedWebHookFired() throws Exception
    {
        final String pluginKey = AddonTestUtils.randomAddOnKey();

        runInRunner(baseUrl, "content_permissions_updated", pluginKey, new WebHookTester()
        {
            @Override
            public void test(WebHookWaiter waiter) throws Exception
            {
                String content = "<h1>Love me</h1>";
                ConfluenceOps.ConfluencePageData pageData = confluenceOps.setPage(some(TestUser.ADMIN), "ds", "testxmlWebhooks", content);
                confluenceOps.addEditRestrictionToPage(some(TestUser.ADMIN), pageData.getId());
                final WebHookBody body = waiter.waitForHook();
                assertNotNull(body);
                Assert.assertEquals(pageData.getId(), body.find("content/id"));
            }
        });
    }
}
