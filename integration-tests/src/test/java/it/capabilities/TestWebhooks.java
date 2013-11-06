package it.capabilities;

import com.atlassian.plugin.connect.test.webhook.WebHookBody;
import com.atlassian.plugin.connect.test.webhook.WebHookTestServlet;
import com.atlassian.plugin.connect.test.webhook.WebHookTester;
import com.atlassian.plugin.connect.test.webhook.WebHookWaiter;
import it.AbstractBrowserlessTest;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

import static org.junit.Assert.assertNotNull;

public class TestWebHooks extends AbstractBrowserlessTest
{
    private static final String WEB_HOOK_PLUGIN_ENABLED = "remote_plugin_enabled";

    @Test
    @Ignore("broken right now since lifecycle webhooks aren't actually firing")
    public void testPluginEnabledWebHookFired() throws Exception
    {
        WebHookTestServlet.runInJsonRunner(baseUrl, WEB_HOOK_PLUGIN_ENABLED, new WebHookTester()
        {
            @Override
            public void test(WebHookWaiter waiter) throws Exception
            {
                final WebHookBody body = waiter.waitForHook();
                assertNotNull(body);
                Assert.assertEquals(WEB_HOOK_PLUGIN_ENABLED, body.find("key"));
            }
        });
    }

}
