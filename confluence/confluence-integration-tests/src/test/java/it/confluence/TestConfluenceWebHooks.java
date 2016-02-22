package it.confluence;

import com.atlassian.confluence.pageobjects.ConfluenceTestedProduct;
import com.atlassian.connect.test.confluence.pageobjects.ConfluenceOps;
import com.atlassian.plugin.connect.test.common.util.AddonTestUtils;
import com.atlassian.plugin.connect.test.common.util.TestUser;
import com.atlassian.plugin.connect.test.common.webhook.WebHookBody;
import com.atlassian.plugin.connect.test.confluence.product.ConfluenceTestedProductAccessor;
import com.atlassian.plugin.connect.test.confluence.util.ConfluenceTestUserFactory;

import org.junit.Test;

import static com.atlassian.fugue.Option.some;
import static com.atlassian.plugin.connect.test.common.matcher.ParamMatchers.isVersionNumber;
import static com.atlassian.plugin.connect.test.common.servlet.WebHookTestServlet.runInRunner;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;

public class TestConfluenceWebHooks {
    protected static final ConfluenceTestedProduct product = new ConfluenceTestedProductAccessor().getConfluenceProduct();

    private final String baseUrl = new ConfluenceTestedProductAccessor().getConfluenceProduct().getProductInstance().getBaseUrl();
    private final ConfluenceTestUserFactory testUserFactory;

    private ConfluenceOps confluenceOps;

    public TestConfluenceWebHooks() {
        confluenceOps = new ConfluenceOps(baseUrl);
        testUserFactory = new ConfluenceTestUserFactory(product);
    }

    @Test
    public void testSearchPerformedWebHookFired() throws Exception {
        final String pluginKey = AddonTestUtils.randomAddonKey();

        runInRunner(baseUrl, "search_performed", pluginKey, waiter -> {
            final String testQuery = "test";
            String results = String.valueOf(
                    confluenceOps.search(some(testUserFactory.basicUser()), testQuery));
            final WebHookBody body = waiter.waitForHook();
            assertNotNull(body);
            assertEquals(testQuery, body.find("query"));
            assertEquals(results, body.find("results"));
        });
    }

    @Test
    public void testPageCreatedWebHookFired() throws Exception {

        final String pluginKey = AddonTestUtils.randomAddonKey();

        runInRunner(baseUrl, "page_created", pluginKey, waiter -> {
            String content = "<h1>Love me</h1>";
            ConfluenceOps.ConfluencePageData pageData = confluenceOps.setPage(some(testUserFactory.basicUser()), "ds", "testxmlWebhooks", content);
            final WebHookBody body = waiter.waitForHook();
            assertNotNull(body);
            assertEquals(pageData.getId(), body.find("page/id"));
            assertEquals(pageData.getCreator(), body.find("page/creatorName"));
        });
    }

    @Test
    public void testVersionIsIncluded() throws Exception {
        final String pluginKey = AddonTestUtils.randomAddonKey();

        runInRunner(baseUrl, "page_created", pluginKey, waiter -> {
            String content = "<h1>I'm a test page</h1>";
            confluenceOps.setPage(some(testUserFactory.basicUser()), "ds", "testxmlWebhooks", content);
            final WebHookBody body = waiter.waitForHook();
            assertNotNull(body);
            assertThat(body.getConnectVersion(), isVersionNumber());
        });
    }

    @Test
    public void testContentPermissionsUpdatedWebHookFired() throws Exception {
        final String pluginKey = AddonTestUtils.randomAddonKey();
        final TestUser user = testUserFactory.basicUser();

        runInRunner(baseUrl, "content_permissions_updated", pluginKey, waiter -> {
            String content = "<h1>Love me</h1>";
            ConfluenceOps.ConfluencePageData pageData = confluenceOps.setPage(some(user), "ds", "testxmlWebhooks", content);
            confluenceOps.addEditRestrictionToPage(some(user), pageData.getId());
            final WebHookBody body = waiter.waitForHook();
            assertNotNull(body);
            assertEquals(pageData.getId(), body.find("content/id"));
        });
    }
}
