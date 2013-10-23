package it;

import java.net.URI;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.atlassian.plugin.connect.plugin.webhooks.PluginsWebHookProvider;
import com.atlassian.plugin.connect.test.server.AtlassianConnectAddOnRunner;
import com.atlassian.plugin.connect.test.server.module.WebhookModule;
import com.atlassian.plugin.connect.test.webhook.WebHookBody;
import com.atlassian.plugin.connect.test.webhook.WebHookTestServlet;
import com.atlassian.plugin.connect.test.webhook.WebHookTester;
import com.atlassian.plugin.connect.test.webhook.WebHookWaiter;

import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

import static com.google.common.collect.Lists.newArrayList;
import static org.junit.Assert.*;

public final class TestWebHooks extends AbstractBrowserlessTest
{
    private static final String WEB_HOOK_PLUGIN_ENABLED = "plugin_enabled";

    @Test
    public void testPluginEnabledWebHookFired() throws Exception
    {
        WebHookTestServlet.runInRunner(baseUrl, WEB_HOOK_PLUGIN_ENABLED, new WebHookTester()
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

    @Test
    public void testRemotePluginInstalledWebHookFired() throws Exception
    {
        testRemotePluginWebHookFired(PluginsWebHookProvider.REMOTE_PLUGIN_INSTALLED);
    }

    @Test
    public void testRemotePluginInstalledSyncCallFired() throws Exception
    {
        testRemotePluginSyncCallFired("install-handler");
    }

    @Test
    public void testRemotePluginEnabledWebHookFired() throws Exception
    {
        testRemotePluginWebHookFired(PluginsWebHookProvider.REMOTE_PLUGIN_ENABLED);
    }

    @Test
    public void testRemotePluginInstalledWebHookFiredOnlyForOwnPlugin() throws Exception
    {
        testRemotePluginWebHookFiredOnlyForOwnPlugin(PluginsWebHookProvider.REMOTE_PLUGIN_INSTALLED);
    }

    @Test
    public void testRemotePluginEnabledWebHookFiredOnlyForOwnPlugin() throws Exception
    {
        testRemotePluginWebHookFiredOnlyForOwnPlugin(PluginsWebHookProvider.REMOTE_PLUGIN_ENABLED);
    }

    //TODO: see ignore comment
    @Ignore("ignoring until plug-core 3.0.5 and plugin disabled events are back in")
    @Test
    public void testRemotePluginDisabledHookFiredOnlyForOwnPlugin() throws Exception
    {
        final WebHookTestServlet servlet = new WebHookTestServlet();
        final String listenerPath = "/webhook";
        final AtlassianConnectAddOnRunner plugin1 = new AtlassianConnectAddOnRunner(baseUrl, PluginsWebHookProvider.REMOTE_PLUGIN_DISABLED)
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
            assertWebHookBody(body, PluginsWebHookProvider.REMOTE_PLUGIN_DISABLED);

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

        final AtlassianConnectAddOnRunner plugin1 = new AtlassianConnectAddOnRunner(baseUrl, webHookId)
                .add(WebhookModule.key(webHookId + path.hashCode())
                                  .path(path)
                                  .event(webHookId)
                                  .resource(servlet));
        final AtlassianConnectAddOnRunner plugin2 = new AtlassianConnectAddOnRunner(baseUrl, "plugin2");
        try
        {
            plugin1.start();
            plugin2.start();

            WebHookBody body = servlet.waitForHook();
            assertNotNull(body);
            Assert.assertEquals(webHookId, body.find("key"));
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
        WebHookTestServlet.runInRunner(baseUrl, webHookId, new WebHookTester()
        {
            @Override
            public void test(WebHookWaiter waiter) throws Exception
            {
                final WebHookBody body = waiter.waitForHook();
                assertWebHookBody(body, webHookId);
            }
        });
    }

    private void testRemotePluginSyncCallFired(final String eventId) throws Exception
    {
        WebHookTestServlet.runSyncInRunner(baseUrl, eventId, new WebHookTester()
        {
            @Override
            public void test(WebHookWaiter waiter) throws Exception
            {
                final WebHookBody body = waiter.waitForHook();
                assertWebHookBody(body, eventId);
                assertUserInUrlParams(body);
                assertUserInBody(body);
            }
        });
    }

    private void assertUserInBody(WebHookBody body) throws Exception
    {
        assertNotNull(body.find("user_id"));
        assertNotNull(body.find("user_key"));
        assertEquals("admin", body.find("user_id"));
    }

    private void assertUserInUrlParams(WebHookBody body) throws Exception
    {
        assertNotNull(body);
        URI uri = body.getRequestURI();

        Map<String,String> params = parseQueryParams(uri.getQuery());
        
        assertTrue(params.containsKey("user_id"));
        assertTrue(params.containsKey("user_key"));
        
        assertEquals("admin",params.get("user_id"));
        
    }

    private Map<String, String> parseQueryParams(String query)
    {
        Map<String,String> params = new HashMap<String, String>();
        String[] nvps = query.split("&");
        
        for(String nvp : nvps)
        {
            String[] param = nvp.split("=");
            String val = "";
            if(param.length > 1)
            {
                val = param[1];
            }
            
            params.put(param[0],val);
        }
        
        return params;
    }

    

    private void assertWebHookBody(final WebHookBody body, final String webHookId) throws Exception
    {
        assertNotNull(body);
        Assert.assertEquals(webHookId, body.find("key"));
        assertNotNull(body.find("clientKey"));
        assertNotNull(body.find("publicKey"));
        assertNotNull(body.find("serverVersion"));
        assertNotNull(body.find("pluginsVersion"));
        assertEquals(baseUrl, body.find("baseUrl"));
        assertNotNull(body.find("productType"));
        assertNotNull(body.find("description"));
    }
}
