package it;

import com.atlassian.plugin.remotable.test.RemotePluginRunner;
import com.atlassian.plugin.remotable.test.webhook.WebHookBody;
import com.atlassian.plugin.remotable.test.webhook.WebHookTestServlet;
import com.atlassian.plugin.remotable.test.webhook.WebHookTester;
import com.atlassian.plugin.remotable.test.webhook.WebHookWaiter;
import org.junit.Test;

import static com.atlassian.plugin.remotable.plugin.webhooks.PluginsWebHookProvider.*;
import static com.atlassian.plugin.remotable.test.webhook.WebHookTestServlet.*;
import static org.junit.Assert.*;

public final class TestWebHooks extends AbstractBrowserlessTest
{
    private static final String WEB_HOOK_PLUGIN_ENABLED = "plugin_enabled";

    @Test
    public void testPluginEnabledWebHookFired() throws Exception
    {
        runInRunner(baseUrl, WEB_HOOK_PLUGIN_ENABLED, new WebHookTester()
        {
            @Override
            public void test(WebHookWaiter waiter) throws Exception
            {
                final WebHookBody body = waiter.waitForHook();
                assertNotNull(body);
                assertEquals(WEB_HOOK_PLUGIN_ENABLED, body.find("key"));
            }
        });
    }

    @Test
    public void testRemotePluginInstalledWebHookFired() throws Exception
    {
        testRemotePluginWebHookFired(REMOTE_PLUGIN_INSTALLED);
    }

    @Test
    public void testRemotePluginEnabledWebHookFired() throws Exception
    {
        testRemotePluginWebHookFired(REMOTE_PLUGIN_ENABLED);
    }

    @Test
    public void testRemotePluginInstalledWebHookFiredOnlyForOwnPlugin() throws Exception
    {
        testRemotePluginWebHookFiredOnlyForOwnPlugin(REMOTE_PLUGIN_INSTALLED);
    }

    @Test
    public void testRemotePluginEnabledWebHookFiredOnlyForOwnPlugin() throws Exception
    {
        testRemotePluginWebHookFiredOnlyForOwnPlugin(REMOTE_PLUGIN_ENABLED);
    }

    private void testRemotePluginWebHookFiredOnlyForOwnPlugin(String webHookId) throws Exception
    {
        final WebHookTestServlet servlet = new WebHookTestServlet();
        final RemotePluginRunner plugin1 = new RemotePluginRunner(baseUrl, webHookId).addWebhook(webHookId, "/webhook", webHookId, servlet);
        final RemotePluginRunner plugin2 = new RemotePluginRunner(baseUrl, "plugin2");
        try
        {
            plugin1.start();
            plugin2.start();

            WebHookBody body = servlet.waitForHook();
            assertNotNull(body);
            assertEquals(webHookId, body.find("key"));
            assertNull(servlet.waitForHook()); // we only can listen for our own plugin, not just any plugin
        }
        finally
        {
            plugin1.stop();
            plugin2.stop();
        }
    }

    private void testRemotePluginWebHookFired(final String webHookId) throws Exception
    {
        runInRunner(baseUrl, webHookId, new WebHookTester()
        {
            @Override
            public void test(WebHookWaiter waiter) throws Exception
            {
                final WebHookBody body = waiter.waitForHook();
                assertNotNull(body);
                assertEquals(webHookId, body.find("key"));
                assertNotNull(body.find("clientKey"));
                assertNotNull(body.find("publicKey"));
                assertNotNull(body.find("serverVersion"));
                assertNotNull(body.find("pluginsVersion"));
                assertEquals(baseUrl, body.find("baseUrl"));
                assertNotNull(body.find("productType"));
                assertNotNull(body.find("description"));
            }
        });
    }
}
