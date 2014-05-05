package it.capabilities;

import com.atlassian.plugin.connect.plugin.installer.ConnectAddonManager;
import com.atlassian.plugin.connect.test.RemotePluginUtils;
import com.atlassian.plugin.connect.test.server.ConnectRunner;
import com.atlassian.plugin.connect.test.webhook.WebHookBody;
import com.atlassian.plugin.connect.test.webhook.WebHookTestServlet;
import com.atlassian.plugin.connect.test.webhook.WebHookTester;
import com.atlassian.plugin.connect.test.webhook.WebHookWaiter;
import it.AbstractBrowserlessTest;
import org.junit.Before;
import org.junit.Test;

import static com.atlassian.plugin.connect.test.RemotePluginUtils.randomWebItemBean;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class TestLifecycle extends AbstractBrowserlessTest
{
    private String pluginKey;
    
    @Before
    public void setup()
    {
        this.pluginKey = RemotePluginUtils.randomPluginKey();    
    }
    
    @Test
    public void testPluginInstalledFired() throws Exception
    {
        WebHookTestServlet.runInstallInJsonRunner(baseUrl, pluginKey, new WebHookTester()
        {
            @Override
            public void test(WebHookWaiter waiter) throws Exception
            {
                final WebHookBody body = waiter.waitForHook();
                assertWebHookDidFire(body, ConnectAddonManager.SyncHandler.INSTALLED.name().toLowerCase(), pluginKey);
            }
        });
    }
    
    @Test
    public void testPluginEnabledFired() throws Exception
    {
        WebHookTestServlet.runEnableInJsonRunner(baseUrl, pluginKey, new WebHookTester()
        {
            @Override
            public void test(WebHookWaiter waiter) throws Exception
            {
                final WebHookBody body = waiter.waitForHook();
                assertWebHookDidFire(body, ConnectAddonManager.SyncHandler.ENABLED.name().toLowerCase(), pluginKey);
            }
        });
    }

    @Test
    public void testPluginDisabledFired() throws Exception
    {
        final WebHookTestServlet servlet = new WebHookTestServlet();
        ConnectRunner plugin1 = new ConnectRunner(baseUrl,pluginKey)
                .addDisableLifecycle()
                .addModule("webItems", randomWebItemBean())
                .setAuthenticationToNone()
                .addRoute(ConnectRunner.DISABLED_PATH, servlet);
        try
        {
            plugin1.start();
            plugin1.uninstall();

            WebHookBody body = servlet.waitForHook();
            assertWebHookDidFire(body, ConnectAddonManager.SyncHandler.DISABLED.name().toLowerCase(), pluginKey);
        }
        finally
        {
            plugin1.stopRunnerServer();
        }
    }

    @Test
    public void testPluginUninstalledFired() throws Exception
    {
        final WebHookTestServlet servlet = new WebHookTestServlet();
        ConnectRunner plugin1 = new ConnectRunner(baseUrl, pluginKey)
                .addUninstallLifecycle()
                .setAuthenticationToNone()
                .addModule("webItems", randomWebItemBean())
                .addRoute(ConnectRunner.UNINSTALLED_PATH, servlet);
        try
        {
            plugin1.start();
            plugin1.uninstall();

            WebHookBody body = servlet.waitForHook();
            assertWebHookDidFire(body, ConnectAddonManager.SyncHandler.UNINSTALLED.name().toLowerCase(), pluginKey);
        }
        finally
        {
            plugin1.stopRunnerServer();
        }
    }

    private void assertWebHookDidFire(WebHookBody body, String eventType, String pluginKey) throws Exception
    {
        assertNotNull(body);
        assertEquals(pluginKey, body.find("key"));
        assertEquals(eventType, body.find("eventType"));
    }

}
