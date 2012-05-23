package it;

import com.atlassian.labs.remoteapps.test.webhook.WebHookBody;
import com.atlassian.labs.remoteapps.test.webhook.WebHookTester;
import com.atlassian.labs.remoteapps.test.webhook.WebHookWaiter;
import org.junit.Test;

import static com.atlassian.labs.remoteapps.test.webhook.WebHookTestServlet.runInRunner;
import static org.junit.Assert.assertEquals;

public class TestWebHooks extends AbstractBrowserlessTest
{
    @Test
	public void testAppStartedWebHookFired() throws Exception
    {
        runInRunner(baseUrl, "remote_app_started", new WebHookTester()
        {
            @Override
            public void test(WebHookWaiter waiter) throws Exception
            {
                WebHookBody body = waiter.waitForHook();
                assertEquals("remote_app_started", body.find("key"));
            }
        });
	}

}
