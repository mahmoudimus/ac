package it.com.atlassian.plugin.connect.installer;

import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.connect.modules.beans.AddOnUrlContext;
import com.atlassian.plugin.connect.modules.beans.AuthenticationBean;
import com.atlassian.plugin.connect.modules.beans.ConnectAddonBean;
import com.atlassian.plugin.connect.modules.beans.WebItemModuleBean;
import com.atlassian.plugin.connect.modules.beans.nested.I18nProperty;
import com.atlassian.plugin.connect.plugin.registry.ConnectAddonRegistry;
import com.atlassian.plugin.connect.testsupport.TestPluginInstaller;
import com.atlassian.plugins.osgi.test.AtlassianPluginsTestRunner;
import it.com.atlassian.plugin.connect.TestAuthenticator;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.servlet.http.HttpServletRequest;

import static com.atlassian.plugin.connect.modules.beans.ConnectAddonBean.newConnectAddonBean;
import static com.atlassian.plugin.connect.modules.beans.WebItemModuleBean.newWebItemBean;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith (AtlassianPluginsTestRunner.class)
public class ConnectAddonRegistryTest
{
    public static final String CONTEXT_PATH = "http://ondemand.com/someProduct";
    public static final String PLUGIN_KEY = "my-plugin";
    public static final String PLUGIN_NAME = "My Plugin";
    public static final String MODULE_NAME = "My Web Item";
    public static final String MODULE_KEY = "my-web-item";
    public static final String BASE_URL = "http://my.connect.addon.com";

    private final ConnectAddonRegistry connectAddonRegistry;
    private final TestPluginInstaller testPluginInstaller;
    private final TestAuthenticator testAuthenticator;

    private HttpServletRequest servletRequest;

    public ConnectAddonRegistryTest(ConnectAddonRegistry connectAddonRegistry, TestPluginInstaller testPluginInstaller, TestAuthenticator testAuthenticator)
    {
        this.connectAddonRegistry = connectAddonRegistry;
        this.testPluginInstaller = testPluginInstaller;
        this.testAuthenticator = testAuthenticator;
    }

    @BeforeClass
    public void setup()
    {
        this.servletRequest = mock(HttpServletRequest.class);
        when(servletRequest.getContextPath()).thenReturn(CONTEXT_PATH);

        testAuthenticator.authenticateUser("admin");
    }

    @Test
    public void baseUrlIsStored() throws Exception
    {
        WebItemModuleBean bean = newWebItemBean()
                .withName(new I18nProperty(MODULE_NAME, ""))
                .withKey(MODULE_KEY)
                .withUrl("/my/addon")
                .withLocation("atl.admin/menu")
                .withContext(AddOnUrlContext.addon)
                .build();

        ConnectAddonBean addon = newConnectAddonBean()
                .withName(PLUGIN_NAME)
                .withKey(PLUGIN_KEY)
                .withBaseurl(BASE_URL)
                .withAuthentication(AuthenticationBean.none())
                .withModules("webItems", bean)
                .build();

        Plugin plugin = null;

        try
        {
            plugin = testPluginInstaller.installAddon(addon);

            assertEquals(BASE_URL, connectAddonRegistry.getBaseUrl(plugin.getKey()));
        }
        finally
        {
            if (null != plugin)
            {
                testPluginInstaller.uninstallAddon(plugin);
            }
        }
    }

    @Test
    public void baseUrlIsRemoved() throws Exception
    {
        WebItemModuleBean bean = newWebItemBean()
                .withName(new I18nProperty(MODULE_NAME, ""))
                .withKey(MODULE_KEY)
                .withUrl("/my/addon")
                .withLocation("atl.admin/menu")
                .withContext(AddOnUrlContext.addon)
                .build();

        ConnectAddonBean addon = newConnectAddonBean()
                .withName(PLUGIN_NAME)
                .withKey(PLUGIN_KEY)
                .withBaseurl(BASE_URL)
                .withAuthentication(AuthenticationBean.none())
                .withModules("webItems", bean)
                .build();

        Plugin plugin = null;

        try
        {
            plugin = testPluginInstaller.installAddon(addon);

            String pluginKey = plugin.getKey();

            assertEquals(BASE_URL, connectAddonRegistry.getBaseUrl(pluginKey));

            testPluginInstaller.uninstallAddon(plugin);
            plugin = null;

            assertEquals("", connectAddonRegistry.getBaseUrl(pluginKey));

        }
        finally
        {
            if (null != plugin)
            {
                testPluginInstaller.uninstallAddon(plugin);
            }
        }
    }
}
