package it.capabilities;

import com.atlassian.plugin.connect.plugin.webhooks.PluginsWebHookProvider;
import com.atlassian.plugin.connect.test.server.AtlassianConnectAddOnRunner;
import com.atlassian.plugin.connect.test.server.ConnectCapabilitiesRunner;
import com.atlassian.plugin.connect.test.server.module.WebhookModule;
import com.atlassian.plugin.connect.test.webhook.WebHookBody;
import com.atlassian.plugin.connect.test.webhook.WebHookTestServlet;
import com.atlassian.plugin.connect.test.webhook.WebHookTester;
import com.atlassian.plugin.connect.test.webhook.WebHookWaiter;
import it.AbstractBrowserlessTest;

import org.hamcrest.Matchers;
import org.junit.Assert;
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
                assertWebHookDidFire(body);
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
                assertWebHookDidFire(body);
            }
        });
    }

    @Test
    public void testPluginDisabledFired() throws Exception
    {
        final WebHookTestServlet servlet = new WebHookTestServlet();
        ConnectCapabilitiesRunner plugin1 = new ConnectCapabilitiesRunner(baseUrl, "lifecycle-plugin")
                .addDisableLifecycle()
                .addRoute(ConnectCapabilitiesRunner.DISABLED_PATH, servlet);
        try
        {
            plugin1.start();
            plugin1.uninstall();

            WebHookBody body = servlet.waitForHook();
            assertWebHookDidFire(body);
        }
        finally
        {
            plugin1.stopRunnerServer();
        }
    }

    //TODO: un-ignore when we figure out how to get a jar manifest for an uninstalled bundle or add a BeforePluginUninstalled event
    @Ignore
    @Test
    public void testPluginUninstalledFired() throws Exception
    {
        final WebHookTestServlet servlet = new WebHookTestServlet();
        ConnectCapabilitiesRunner plugin1 = new ConnectCapabilitiesRunner(baseUrl, "lifecycle-plugin")
                .addUninstallLifecycle()
                .addRoute(ConnectCapabilitiesRunner.UNINSTALLED_PATH, servlet);
        try
        {
            plugin1.start();
            plugin1.uninstall();

            WebHookBody body = servlet.waitForHook();
            assertWebHookDidFire(body);
        }
        finally
        {
            plugin1.stopRunnerServer();
        }
    }

    private void assertWebHookDidFire(WebHookBody body) throws Exception
    {
        assertNotNull(body);
        Assert.assertEquals("lifecycle-plugin", body.find("key"));
    }

}
