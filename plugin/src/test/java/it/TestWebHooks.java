package it;

import com.atlassian.labs.remoteapps.test.PluginManagerPage;
import com.atlassian.labs.remoteapps.test.RemoteAppRunner;
import com.atlassian.labs.remoteapps.test.RemoteAppTestPage;
import com.atlassian.pageobjects.page.LoginPage;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Test;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

import static com.atlassian.labs.remoteapps.test.RemoteAppUtils.waitForEvent;
import static com.atlassian.labs.remoteapps.test.Utils.getJson;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class TestWebHooks extends AbstractRemoteAppTest
{
    @Test
	public void testAppStartedWebHookFired() throws Exception, JSONException, InterruptedException
    {
        MyServlet servlet = new MyServlet();
        RemoteAppRunner runner = new RemoteAppRunner(product.getProductInstance().getBaseUrl(),
                "startedHook")
                .addWebhook("remote_app_started", "/started", servlet)
                .start();

        long expiry = System.currentTimeMillis() + 5 * 1000;

        while (expiry > System.currentTimeMillis())
        {
            if (servlet.hookFired())
            {
                break;
            }
            Thread.sleep(100);
        }
        if (!servlet.hookFired())
        {
            throw new AssertionError("Event not published");
        }

        runner.stop();
	}

    public static class MyServlet extends HttpServlet
    {
        private volatile boolean fired;
        @Override
        protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws
                ServletException,
                IOException
        {
            if (req.getRequestURI().endsWith("/started"))
            {
                fired = true;
            }
        }

        public boolean hookFired()
        {
            return fired;
        }
    }
}
