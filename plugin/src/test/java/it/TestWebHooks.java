package it;

import com.atlassian.plugin.remotable.test.webhook.WebHookBody;
import com.atlassian.plugin.remotable.test.webhook.WebHookTester;
import com.atlassian.plugin.remotable.test.webhook.WebHookWaiter;
import org.junit.Test;

import static com.atlassian.plugin.remotable.test.webhook.WebHookTestServlet.runInRunner;
import static org.junit.Assert.assertEquals;

public class TestWebHooks extends AbstractBrowserlessTest
{
    @Test
	public void testAppStartedWebHookFired() throws Exception
    {
        runInRunner(baseUrl, "plugin_enabled", new WebHookTester()
        {
            @Override
            public void test(WebHookWaiter waiter) throws Exception
            {
                WebHookBody body = waiter.waitForHook();
                assertEquals("plugin_enabled", body.find("key"));
            }
        });
	}
}
