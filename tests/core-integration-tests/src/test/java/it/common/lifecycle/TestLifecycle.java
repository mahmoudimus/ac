package it.common.lifecycle;

import com.atlassian.plugin.connect.plugin.lifecycle.ConnectAddonManager;
import com.atlassian.plugin.connect.test.common.servlet.ConnectRunner;
import com.atlassian.plugin.connect.test.common.servlet.WebHookTestServlet;
import com.atlassian.plugin.connect.test.common.util.AddonTestUtils;
import com.atlassian.plugin.connect.test.common.webhook.WebHookBody;
import com.atlassian.plugin.connect.test.product.TestedProductAccessor;
import org.junit.Before;
import org.junit.Test;

import static com.atlassian.plugin.connect.test.common.util.AddonTestUtils.randomWebItemBean;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class TestLifecycle {
    private final String baseUrl = TestedProductAccessor.get().getTestedProduct().getProductInstance().getBaseUrl();
    private String pluginKey;

    @Before
    public void setup() {
        this.pluginKey = AddonTestUtils.randomAddonKey();
    }

    @Test
    public void testPluginInstalledFired() throws Exception {
        WebHookTestServlet.runInstallInJsonRunner(baseUrl, pluginKey, waiter -> {
            final WebHookBody body = waiter.waitForHook();
            assertWebHookDidFire(body, ConnectAddonManager.SyncHandler.INSTALLED.name().toLowerCase(), pluginKey);
        });
    }

    @Test
    public void testPluginEnabledFired() throws Exception {
        WebHookTestServlet.runEnableInJsonRunner(baseUrl, pluginKey, waiter -> {
            final WebHookBody body = waiter.waitForHook();
            assertWebHookDidFire(body, ConnectAddonManager.SyncHandler.ENABLED.name().toLowerCase(), pluginKey);
        });
    }

    @Test
    public void testPluginDisabledFired() throws Exception {
        final WebHookTestServlet servlet = new WebHookTestServlet();
        ConnectRunner plugin1 = new ConnectRunner(baseUrl, pluginKey)
                .addDisableLifecycle()
                .addModule("webItems", randomWebItemBean())
                .setAuthenticationToNone()
                .addRoute(ConnectRunner.DISABLED_PATH, servlet);
        try {
            plugin1.start();
            plugin1.uninstall();

            WebHookBody body = servlet.waitForHook();
            assertWebHookDidFire(body, ConnectAddonManager.SyncHandler.DISABLED.name().toLowerCase(), pluginKey);
        } finally {
            plugin1.stopRunnerServer();
        }
    }

    @Test
    public void testPluginUninstalledFired() throws Exception {
        final WebHookTestServlet servlet = new WebHookTestServlet();
        ConnectRunner plugin1 = new ConnectRunner(baseUrl, pluginKey)
                .addUninstallLifecycle()
                .setAuthenticationToNone()
                .addModule("webItems", randomWebItemBean())
                .addRoute(ConnectRunner.UNINSTALLED_PATH, servlet);
        try {
            plugin1.start();
            plugin1.uninstall();

            WebHookBody body = servlet.waitForHook();
            assertWebHookDidFire(body, ConnectAddonManager.SyncHandler.UNINSTALLED.name().toLowerCase(), pluginKey);
        } finally {
            plugin1.stopRunnerServer();
        }
    }

    private void assertWebHookDidFire(WebHookBody body, String eventType, String pluginKey) throws Exception {
        assertNotNull(body);
        assertEquals(pluginKey, body.find("key"));
        assertEquals(eventType, body.find("eventType"));
    }

}
