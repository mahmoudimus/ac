package it.com.atlassian.plugin.connect.plugin.installer;

import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.PluginState;
import com.atlassian.plugin.connect.modules.beans.AuthenticationBean;
import com.atlassian.plugin.connect.modules.beans.AuthenticationType;
import com.atlassian.plugin.connect.modules.beans.ConnectAddonBean;
import com.atlassian.plugin.connect.modules.beans.LifecycleBean;
import com.atlassian.plugin.connect.testsupport.TestPluginInstaller;
import com.atlassian.plugins.osgi.test.AtlassianPluginsTestRunner;
import com.atlassian.plugin.connect.testsupport.util.auth.TestAuthenticator;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.IOException;

import static com.atlassian.plugin.connect.testsupport.util.AddonUtil.randomWebItemBean;
import static org.junit.Assert.assertEquals;

@RunWith(AtlassianPluginsTestRunner.class)
public class AddOnUpgradeTest
{
    private final TestPluginInstaller testPluginInstaller;
    private final TestAuthenticator testAuthenticator;

    private Plugin plugin;
    private ConnectAddonBean addonBean;

    public AddOnUpgradeTest(TestPluginInstaller testPluginInstaller, TestAuthenticator testAuthenticator)
    {
        this.testPluginInstaller = testPluginInstaller;
        this.testAuthenticator = testAuthenticator;
    }

    @Before
    public void setUp() throws IOException
    {
        testAuthenticator.authenticateUser("admin");
        plugin = installPlugin();
    }

    @After
    public void tearDown() throws IOException
    {
        if (null != plugin)
        {
            testPluginInstaller.uninstallAddon(plugin);
        }
    }

    @Test
    public void upgradedDisabledAddonRemainsDisabled() throws IOException
    {
        testPluginInstaller.disableAddon(plugin.getKey());
        Plugin upgradedPlugin = testPluginInstaller.installAddon(addonBean);
        assertEquals(PluginState.DISABLED, upgradedPlugin.getPluginState());
    }

    @Test
    public void upgradedEnabledAddonRemainsEnabled() throws IOException
    {
        testPluginInstaller.enableAddon(plugin.getKey());
        Plugin upgradedPlugin = testPluginInstaller.installAddon(addonBean);
        assertEquals(PluginState.ENABLED, upgradedPlugin.getPluginState());
    }

    private Plugin installPlugin() throws IOException
    {
        String key = "ac-test-" + System.currentTimeMillis();
        addonBean = ConnectAddonBean.newConnectAddonBean()
                .withKey(key)
                .withDescription(AddOnUpgradeTest.class.getCanonicalName())
                .withBaseurl(testPluginInstaller.getInternalAddonBaseUrl(key))
                .withAuthentication(AuthenticationBean.newAuthenticationBean().withType(AuthenticationType.JWT).build())
                .withLifecycle(LifecycleBean.newLifecycleBean().withInstalled("/installed").build())
                .withModule("webItems", randomWebItemBean())
                .build();

        return testPluginInstaller.installAddon(addonBean);
    }
}
