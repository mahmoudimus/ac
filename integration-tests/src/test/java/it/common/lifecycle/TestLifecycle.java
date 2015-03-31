package it.common.lifecycle;

import com.atlassian.plugin.connect.plugin.capabilities.ConvertToWiredTest;
import com.atlassian.plugin.connect.plugin.installer.ConnectAddonManager;
import com.atlassian.plugin.connect.test.AddonTestUtils;
import com.atlassian.plugin.connect.test.BaseUrlLocator;
import com.atlassian.plugin.connect.test.server.ConnectRunner;
import com.atlassian.plugin.connect.test.webhook.WebHookBody;
import com.atlassian.plugin.connect.test.webhook.WebHookTestServlet;
import com.atlassian.plugin.connect.test.webhook.WebHookTester;
import com.atlassian.plugin.connect.test.webhook.WebHookWaiter;

import org.junit.Before;
import org.junit.Test;

import static com.atlassian.plugin.connect.test.AddonTestUtils.randomWebItemBean;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

@ConvertToWiredTest
public class TestLifecycle
{
    private final String baseUrl = BaseUrlLocator.getBaseUrl();
    private String pluginKey;

    @Before
    public void setup()
    {
        this.pluginKey = AddonTestUtils.randomAddOnKey();
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