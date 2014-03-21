package it.capabilities;

import com.atlassian.plugin.connect.plugin.installer.ConnectAddonManager;
import com.atlassian.plugin.connect.test.server.ConnectRunner;
import com.atlassian.plugin.connect.test.webhook.WebHookBody;
import com.atlassian.plugin.connect.test.webhook.WebHookTestServlet;
import com.atlassian.plugin.connect.test.webhook.WebHookTester;
import com.atlassian.plugin.connect.test.webhook.WebHookWaiter;
import it.AbstractBrowserlessTest;

import org.junit.Ignore;
import org.junit.Test;

import static org.junit.Assert.*;

public class TestLifecycle extends AbstractBrowserlessTest
{
    @Test
    public void testPluginInstalledFired() throws Exception
    {
        WebHookTestServlet.runInstallInJsonRunner(baseUrl, new WebHookTester()
        {
            @Override
            public void test(WebHookWaiter waiter) throws Exception
            {
                final WebHookBody body = waiter.waitForHook();
                assertWebHookDidFire(body, ConnectAddonManager.SyncHandler.INSTALLED.name().toLowerCase());
            }
        });
    }
    
    @Test
    public void testPluginEnabledFired() throws Exception
    {
        WebHookTestServlet.runEnableInJsonRunner(baseUrl, new WebHookTester()
        {
            @Override
            public void test(WebHookWaiter waiter) throws Exception
            {
                final WebHookBody body = waiter.waitForHook();
                assertWebHookDidFire(body, ConnectAddonManager.SyncHandler.ENABLED.name().toLowerCase());
            }
        });
    }

    @Test
    public void testPluginDisabledFired() throws Exception
    {
        final WebHookTestServlet servlet = new WebHookTestServlet();
        ConnectRunner plugin1 = new ConnectRunner(baseUrl, "lifecycle-plugin")
                .addDisableLifecycle()
                .setAuthenticationToNone()
                .addRoute(ConnectRunner.DISABLED_PATH, servlet);
        try
        {
            plugin1.start();
            plugin1.uninstall();

            WebHookBody body = servlet.waitForHook();
            assertWebHookDidFire(body, ConnectAddonManager.SyncHandler.DISABLED.name().toLowerCase());
        }
        finally
        {
            plugin1.stopRunnerServer();
        }
    }

    //TODO: un-ignore when we figure out how to sign a request after a plugin has been uninstalled or add a BeforePluginUninstalled event
    @Ignore
    @Test
    public void testPluginUninstalledFired() throws Exception
    {
        final WebHookTestServlet servlet = new WebHookTestServlet();
        ConnectRunner plugin1 = new ConnectRunner(baseUrl, "lifecycle-plugin")
                .addUninstallLifecycle()
                .addRoute(ConnectRunner.UNINSTALLED_PATH, servlet);
        try
        {
            plugin1.start();
            plugin1.uninstall();

            WebHookBody body = servlet.waitForHook();
            assertWebHookDidFire(body, ConnectAddonManager.SyncHandler.UNINSTALLED.name().toLowerCase());
        }
        finally
        {
            plugin1.stopRunnerServer();
        }
    }

    private void assertWebHookDidFire(WebHookBody body, String eventType) throws Exception
    {
        assertNotNull(body);
        assertEquals("lifecycle-plugin", body.find("key"));
        assertEquals(eventType, body.find("eventType"));
    }

}
