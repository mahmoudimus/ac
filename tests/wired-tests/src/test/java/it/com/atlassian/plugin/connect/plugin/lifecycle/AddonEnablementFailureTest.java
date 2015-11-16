package it.com.atlassian.plugin.connect.plugin.lifecycle;

import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.connect.modules.beans.AuthenticationBean;
import com.atlassian.plugin.connect.modules.beans.ConnectAddonBean;
import com.atlassian.plugin.connect.modules.beans.nested.I18nProperty;
import com.atlassian.plugin.connect.testsupport.TestPluginInstaller;
import com.atlassian.plugin.connect.testsupport.util.AddonUtil;
import com.atlassian.plugins.osgi.test.AtlassianPluginsTestRunner;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.IOException;

import static com.atlassian.plugin.connect.modules.beans.ConnectAddonBean.newConnectAddonBean;
import static com.atlassian.plugin.connect.modules.beans.WebItemModuleBean.newWebItemBean;
import static com.atlassian.plugin.connect.modules.beans.nested.SingleConditionBean.newSingleConditionBean;

@RunWith(AtlassianPluginsTestRunner.class)
public class AddonEnablementFailureTest
{

    private final TestPluginInstaller pluginInstaller;

    private Plugin plugin;

    public AddonEnablementFailureTest(TestPluginInstaller pluginInstaller)
    {
        this.pluginInstaller = pluginInstaller;
    }

    @Before
    public void setUp() throws IOException
    {
        ConnectAddonBean addon = newAddonToFailEnablement();
        plugin = pluginInstaller.installAddon(addon);
    }

    @After
    public void tearDown() throws IOException
    {
        if (plugin != null)
        {
            pluginInstaller.uninstallAddon(plugin);
            plugin = null;
        }
    }

    @Test
    public void shouldSkipAddonEnablementUponPluginModuleRegistrationError() throws IOException
    {
        pluginInstaller.enableAddon(plugin.getKey());
    }

    private ConnectAddonBean newAddonToFailEnablement()
    {
        return newConnectAddonBean()
                .withKey(AddonUtil.randomPluginKey())
                .withBaseurl("http://example.com")
                .withAuthentication(AuthenticationBean.none())
                .withModule("webItems", newWebItemBean()
                        .withKey("will-fail-enablement")
                        .withUrl("/")
                        .withName(new I18nProperty("Will fail enablement", null))
                        .withConditions(newSingleConditionBean().withCondition("feature_flag").build())
                        .withLocation("some-location")
                        .build())
                .build();
    }
}
