package it;

import com.atlassian.plugin.connect.api.xmldescriptor.XmlDescriptor;
import com.atlassian.plugin.connect.plugin.capabilities.ConvertToWiredTest;
import com.atlassian.plugin.connect.plugin.webhooks.PluginsWebHookProvider;
import com.atlassian.plugin.connect.test.AddonTestUtils;
import com.atlassian.plugin.connect.test.server.AtlassianConnectAddOnRunner;
import com.atlassian.plugin.connect.test.server.module.WebhookModule;
import com.atlassian.plugin.connect.test.webhook.WebHookBody;
import com.atlassian.plugin.connect.test.webhook.WebHookTestServlet;
import com.atlassian.plugin.connect.test.webhook.WebHookTester;
import com.atlassian.plugin.connect.test.webhook.WebHookWaiter;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

import static org.junit.Assert.*;

@ConvertToWiredTest
public final class TestWebHooks extends AbstractBrowserlessTest
{
    private static final String WEB_HOOK_PLUGIN_ENABLED = "plugin_enabled";

    @Test
    public void testPluginEnabledWebHookFired() throws Exception
    {
        final String pluginKey = AddonTestUtils.randomAddOnKey();
        
        WebHookTestServlet.runInRunner(baseUrl, WEB_HOOK_PLUGIN_ENABLED, pluginKey, new WebHookTester()
        {
            @Override
            public void test(WebHookWaiter waiter) throws Exception
            {
                final WebHookBody body = waiter.waitForHook();
                assertNotNull(body);
                Assert.assertEquals(pluginKey, body.find("key"));
            }
        });
    }

    @Test
    public void testRemotePluginEnabledWebHookFired() throws Exception
    {
        testRemotePluginWebHookFired(PluginsWebHookProvider.CONNECT_ADDON_ENABLED);
    }

    @Test
    public void testRemotePluginEnabledWebHookFiredOnlyForOwnPlugin() throws Exception
    {
        testRemotePluginWebHookFiredOnlyForOwnPlugin(PluginsWebHookProvider.CONNECT_ADDON_ENABLED);
    }

    //TODO: see ignore comment
    @Ignore("ignoring until plug-core 3.0.5 and plugin disabled events are back in")
    @Test
    @XmlDescriptor
    public void testRemotePluginDisabledHookFiredOnlyForOwnPlugin() throws Exception
    {
        final WebHookTestServlet servlet = new WebHookTestServlet();
        final String listenerPath = "/webhook";
        final AtlassianConnectAddOnRunner plugin1 = new AtlassianConnectAddOnRunner(baseUrl)
                .add(WebhookModule.key(PluginsWebHookProvider.REMOTE_PLUGIN_DISABLED + listenerPath.hashCode())
                        .path(listenerPath)
                        .event(PluginsWebHookProvider.REMOTE_PLUGIN_DISABLED)
                        .resource(servlet));
        final AtlassianConnectAddOnRunner plugin2 = new AtlassianConnectAddOnRunner(baseUrl, "plugin2");
        try
        {
            plugin1.start();
            plugin2.start();
            plugin1.uninstall();

            WebHookBody body = servlet.waitForHook();
            assertWebHookBody(body, PluginsWebHookProvider.REMOTE_PLUGIN_DISABLED, plugin1.getPluginKey());

            plugin2.uninstall();
            assertNull(servlet.waitForHook());
        }
        finally
        {
            plugin1.stopRunnerServer();
            plugin2.stopRunnerServer();
        }
    }

    private void testRemotePluginWebHookFiredOnlyForOwnPlugin(String webHookId) throws Exception
    {
        final String path = "/webhook";
        final WebHookTestServlet servlet = new WebHookTestServlet();

        final AtlassianConnectAddOnRunner plugin1 = new AtlassianConnectAddOnRunner(baseUrl)
                .add(WebhookModule.key(webHookId + path.hashCode())
                                  .path(path)
                                  .event(webHookId)
                                  .resource(servlet));
        final AtlassianConnectAddOnRunner plugin2 = new AtlassianConnectAddOnRunner(baseUrl);
        try
        {
            plugin1.start();
            plugin2.start();

            WebHookBody body = servlet.waitForHook();
            assertNotNull(body);
            Assert.assertEquals(plugin1.getPluginKey(), body.find("key"));
            assertNull(servlet.waitForHook()); // we only can listen for our own plugin, not just any plugin
        }
        finally
        {
            plugin1.stopAndUninstall();
            plugin2.stopAndUninstall();
        }
    }

    private void testRemotePluginWebHookFired(final String webHookId) throws Exception
    {
        final String pluginKey = AddonTestUtils.randomAddOnKey();
        
        WebHookTestServlet.runInRunner(baseUrl, webHookId, pluginKey, new WebHookTester()
        {
            @Override
            public void test(WebHookWaiter waiter) throws Exception
            {
                final WebHookBody body = waiter.waitForHook();
                assertWebHookBody(body, webHookId, pluginKey);
            }
        });
    }

    private void assertWebHookBody(final WebHookBody body, final String webHookId, final String pluginKey) throws Exception
    {
        assertNotNull(body);
        Assert.assertEquals(pluginKey, body.find("key"));
        assertNotNull(body.find("clientKey"));
        assertNotNull(body.find("publicKey"));
        assertNotNull(body.find("serverVersion"));
        assertNotNull(body.find("pluginsVersion"));
        assertEquals(baseUrl, body.find("baseUrl"));
        assertNotNull(body.find("productType"));
        assertNotNull(body.find("description"));
    }
}
