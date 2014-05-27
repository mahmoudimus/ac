package it.com.atlassian.plugin.connect.provider;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import javax.servlet.http.HttpServletRequest;

import com.atlassian.plugin.ModuleDescriptor;
import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.PluginAccessor;
import com.atlassian.plugin.connect.modules.beans.AddOnUrlContext;
import com.atlassian.plugin.connect.modules.beans.AuthenticationBean;
import com.atlassian.plugin.connect.modules.beans.ConnectAddonBean;
import com.atlassian.plugin.connect.modules.beans.WebItemModuleBean;
import com.atlassian.plugin.connect.modules.beans.WebItemTargetType;
import com.atlassian.plugin.connect.modules.beans.nested.I18nProperty;
import com.atlassian.plugin.connect.modules.util.ModuleKeyUtils;
import com.atlassian.plugin.connect.plugin.ConnectPluginInfo;
import com.atlassian.plugin.connect.plugin.capabilities.provider.WebItemModuleProvider;
import com.atlassian.plugin.connect.testsupport.TestPluginInstaller;
import com.atlassian.plugin.util.WaitUntil;
import com.atlassian.plugin.web.descriptors.WebItemModuleDescriptor;
import com.atlassian.plugins.osgi.test.AtlassianPluginsTestRunner;

import it.com.atlassian.plugin.connect.TestAuthenticator;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

import static com.atlassian.plugin.connect.modules.beans.ConnectAddonBean.newConnectAddonBean;
import static com.atlassian.plugin.connect.modules.beans.WebItemModuleBean.newWebItemBean;
import static com.atlassian.plugin.connect.modules.beans.WebItemTargetBean.newWebItemTargetBean;
import static com.atlassian.plugin.connect.modules.util.ModuleKeyUtils.addonAndModuleKey;
import static com.atlassian.plugin.connect.test.util.AddonUtil.randomPluginKey;
import static com.google.common.collect.Lists.newArrayList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith (AtlassianPluginsTestRunner.class)
public class WebItemModuleProviderTest
{
    public static final String PLUGIN_NAME = "My Plugin";
    public static final String MODULE_NAME = "My Web Item";
    public static final String MODULE_KEY = "my-web-item";
    public static final String OTHER_MODULE_NAME = "My Other Web Item";
    public static final String OTHER_MODULE_KEY = "my-other-web-item";
    public static final String CONTEXT_PATH = "http://ondemand.com/someProduct";
    public static final String BASE_URL = "https://my.connect.addon.com";
    public static final String VELOCITY_LABEL = "My $var is ${awesome}";
    public static final String VELOCITY_TOOLTIP = "My tooltip $var is ${awesome}";

    private final WebItemModuleProvider webItemModuleProvider;
    private final TestPluginInstaller testPluginInstaller;
    private final TestAuthenticator testAuthenticator;
    private final PluginAccessor pluginAccessor;
    private HttpServletRequest servletRequest;
    private ConnectAddonBean addon;
    
    private String pluginKey;

    public WebItemModuleProviderTest(WebItemModuleProvider webItemModuleProvider, TestPluginInstaller testPluginInstaller,
                                     TestAuthenticator testAuthenticator, PluginAccessor pluginAccessor)
    {
        this.webItemModuleProvider = webItemModuleProvider;
        this.testPluginInstaller = testPluginInstaller;
        this.testAuthenticator = testAuthenticator;
        this.pluginAccessor = pluginAccessor;
    }

    @BeforeClass
    public void authenticate()
    {
        testAuthenticator.authenticateUser("admin");
    }

    @Before
    public void setup()
    {
        this.pluginKey = randomPluginKey();
        this.addon = newConnectAddonBean().withKey(pluginKey).build();
        this.servletRequest = mock(HttpServletRequest.class);
        when(servletRequest.getContextPath()).thenReturn(CONTEXT_PATH);
    }

    @Test
    public void singleAbsoluteLink() throws Exception
    {
        WebItemModuleBean bean = newWebItemBean()
                .withName(new I18nProperty(MODULE_NAME, ""))
                .withKey(MODULE_KEY)
                .withUrl("http://www.google.com")
                .withLocation("atl.admin/menu")
                .build();

        Plugin plugin = getConnectPlugin();
        List<ModuleDescriptor> descriptors = webItemModuleProvider.provideModules(addon, plugin, "webItems", newArrayList(bean));

        assertEquals(1, descriptors.size());

        WebItemModuleDescriptor descriptor = (WebItemModuleDescriptor) descriptors.get(0);
        descriptor.enabled();

        assertEquals("http://www.google.com", descriptor.getLink().getDisplayableUrl(servletRequest, new HashMap<String, Object>()));
    }

    @Test
    public void singleAbsoluteLinkWithPageContext() throws Exception
    {
        WebItemModuleBean bean = newWebItemBean()
                .withName(new I18nProperty(MODULE_NAME, ""))
                .withKey(MODULE_KEY)
                .withUrl("http://www.google.com")
                .withLocation("atl.admin/menu")
                .withContext(AddOnUrlContext.page)
                .build();

        Plugin plugin = getConnectPlugin();
        List<ModuleDescriptor> descriptors = webItemModuleProvider.provideModules(addon, plugin, "webItems", newArrayList(bean));

        assertEquals(1, descriptors.size());

        WebItemModuleDescriptor descriptor = (WebItemModuleDescriptor) descriptors.get(0);
        descriptor.enabled();

        assertEquals("http://www.google.com", descriptor.getLink().getDisplayableUrl(servletRequest, new HashMap<String, Object>()));
    }

    @Test
    public void singleAbsoluteLinkWithProductContext() throws Exception
    {
        WebItemModuleBean bean = newWebItemBean()
                .withName(new I18nProperty(MODULE_NAME, ""))
                .withKey(MODULE_KEY)
                .withUrl("http://www.google.com")
                .withLocation("atl.admin/menu")
                .withContext(AddOnUrlContext.product)
                .build();

        Plugin plugin = getConnectPlugin();
        List<ModuleDescriptor> descriptors = webItemModuleProvider.provideModules(addon, plugin, "webItems", newArrayList(bean));

        assertEquals(1, descriptors.size());

        WebItemModuleDescriptor descriptor = (WebItemModuleDescriptor) descriptors.get(0);
        descriptor.enabled();

        assertEquals("http://www.google.com", descriptor.getLink().getDisplayableUrl(servletRequest, new HashMap<String, Object>()));
    }

    @Test
    public void singlePageLink() throws Exception
    {
        WebItemModuleBean bean = newWebItemBean()
                .withName(new I18nProperty(MODULE_NAME, ""))
                .withKey(MODULE_KEY)
                .withUrl("some-page-key")
                .withLocation("atl.admin/menu")
                .withContext(AddOnUrlContext.page)
                .build();

        Plugin plugin = getConnectPlugin();
        List<ModuleDescriptor> descriptors = webItemModuleProvider.provideModules(addon, plugin, "webItems", newArrayList(bean));

        assertEquals(1, descriptors.size());

        WebItemModuleDescriptor descriptor = (WebItemModuleDescriptor) descriptors.get(0);
        descriptor.enabled();

        assertEquals(CONTEXT_PATH + "/plugins/servlet/ac/" + addon.getKey() + "/some-page-key", descriptor.getLink().getDisplayableUrl(servletRequest, new HashMap<String, Object>()));
    }

    @Test
    public void singlePageLinkName() throws Exception
    {
        WebItemModuleBean bean = newWebItemBean()
                .withName(new I18nProperty(MODULE_NAME, ""))
                .withKey(MODULE_KEY)
                .withUrl("some-page-key")
                .withLocation("atl.admin/menu")
                .withContext(AddOnUrlContext.page)
                .build();

        ConnectAddonBean addon = newConnectAddonBean()
                .withName(PLUGIN_NAME)
                .withKey(pluginKey)
                .withBaseurl(BASE_URL)
                .withAuthentication(AuthenticationBean.none())
                .withModules("webItems", bean)
                .build();

        Plugin plugin = null;

        try
        {
            plugin = testPluginInstaller.installAddon(addon);

            final Plugin connectPlugin = getConnectPlugin();
            final String moduleKey = addonAndModuleKey(pluginKey,MODULE_KEY);

            WaitUntil.invoke(new WaitUntil.WaitCondition() {
                @Override
                public boolean isFinished()
                {
                    return null != connectPlugin.getModuleDescriptor(moduleKey);
                }

                @Override
                public String getWaitMessage()
                {
                    return "waiting for addon module to be registered...";
                }
            });
            
            WebItemModuleDescriptor descriptor = (WebItemModuleDescriptor) connectPlugin.getModuleDescriptor(moduleKey);

            assertEquals(MODULE_NAME,descriptor.getWebLabel().getDisplayableLabel(mock(HttpServletRequest.class),new HashMap<String, Object>()));
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
    public void singleProductLink() throws Exception
    {
        WebItemModuleBean bean = newWebItemBean()
                .withName(new I18nProperty(MODULE_NAME, ""))
                .withKey(MODULE_KEY)
                .withUrl("/local/jira/admin")
                .withLocation("atl.admin/menu")
                .withContext(AddOnUrlContext.product)
                .build();

        Plugin plugin = getConnectPlugin();
        List<ModuleDescriptor> descriptors = webItemModuleProvider.provideModules(addon, plugin, "webItems", newArrayList(bean));

        assertEquals(1, descriptors.size());

        WebItemModuleDescriptor descriptor = (WebItemModuleDescriptor) descriptors.get(0);
        descriptor.enabled();

        assertEquals(CONTEXT_PATH + "/local/jira/admin", descriptor.getLink().getDisplayableUrl(servletRequest, new HashMap<String, Object>()));
    }

    @Test
    public void singleAddonLink() throws Exception
    {
        WebItemModuleBean bean = newWebItemBean()
                .withName(new I18nProperty(MODULE_NAME, ""))
                .withKey(MODULE_KEY)
                .withUrl("/my/addon")
                .withLocation("atl.admin/menu")
                .build();

        ConnectAddonBean addon = newConnectAddonBean()
                .withName(PLUGIN_NAME)
                .withKey(pluginKey)
                .withBaseurl(BASE_URL)
                .withAuthentication(AuthenticationBean.none())
                .withModules("webItems", bean)
                .build();

        Plugin plugin = null;

        try
        {
            plugin = testPluginInstaller.installAddon(addon);

            List<ModuleDescriptor> descriptors = webItemModuleProvider.provideModules(addon, getConnectPlugin(), "webItems", newArrayList(bean));

            assertEquals(1, descriptors.size());

            WebItemModuleDescriptor descriptor = (WebItemModuleDescriptor) descriptors.get(0);
            descriptor.enabled();

            assertTrue(descriptor.getLink().getDisplayableUrl(servletRequest, new HashMap<String, Object>()).startsWith(BASE_URL + "/my/addon"));
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
    public void dialogOptions() throws Exception
    {
        WebItemModuleBean bean = newWebItemBean()
                .withName(new I18nProperty(MODULE_NAME, ""))
                .withKey(MODULE_KEY)
                .withUrl("/my/addon")
                .withLocation("atl.admin/menu")
                .withTarget(
                        newWebItemTargetBean()
                                .withType(WebItemTargetType.dialog)
                                .withOption("width", "100")
                                .withOption("height", "300px")
                                .withOption("onHover", true)
                                .build()
                )
                .build();

        ConnectAddonBean addon = newConnectAddonBean()
                .withName(PLUGIN_NAME)
                .withKey(pluginKey)
                .withAuthentication(AuthenticationBean.none())
                .withBaseurl(BASE_URL)
                .withModules("webItems", bean)
                .build();

        Plugin plugin = null;

        try
        {
            plugin = testPluginInstaller.installAddon(addon);

            List<ModuleDescriptor> descriptors = webItemModuleProvider.provideModules(addon, getConnectPlugin(), "webItems", newArrayList(bean));

            assertEquals(1, descriptors.size());

            WebItemModuleDescriptor descriptor = (WebItemModuleDescriptor) descriptors.get(0);
            descriptor.enabled();

            assertTrue(descriptor.getLink().getDisplayableUrl(servletRequest, new HashMap<String, Object>()).startsWith(BASE_URL + "/my/addon"));
            assertTrue("expected param [-acopt-width]", descriptor.getParams().containsKey("-acopt-width"));
            assertTrue("expected param [-acopt-height]", descriptor.getParams().containsKey("-acopt-height"));
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
    public void multipleWebItems() throws Exception
    {
        WebItemModuleBean bean = newWebItemBean()
                .withName(new I18nProperty(MODULE_NAME, ""))
                .withKey(MODULE_KEY)
                .withUrl("http://www.google.com")
                .withLocation("atl.admin/menu")
                .build();

        WebItemModuleBean bean2 = newWebItemBean()
                .withName(new I18nProperty(OTHER_MODULE_NAME, ""))
                .withKey(OTHER_MODULE_KEY)
                .withUrl("/my/addon")
                .withLocation("atl.admin/menu")
                .build();

        ConnectAddonBean addon = newConnectAddonBean()
                .withName(PLUGIN_NAME)
                .withKey(pluginKey)
                .withBaseurl(BASE_URL)
                .withAuthentication(AuthenticationBean.none())
                .withModules("webItems", bean, bean2)
                .build();

        Plugin plugin = null;

        try
        {
            plugin = testPluginInstaller.installAddon(addon);

            List<ModuleDescriptor> descriptors = webItemModuleProvider.provideModules(addon, getConnectPlugin(), "webItems", newArrayList(bean, bean2));

            assertEquals(2, descriptors.size());

            WebItemModuleDescriptor descriptor = (WebItemModuleDescriptor) descriptors.get(0);
            descriptor.enabled();

            WebItemModuleDescriptor descriptor2 = (WebItemModuleDescriptor) descriptors.get(1);
            descriptor2.enabled();

            assertEquals("http://www.google.com", descriptor.getLink().getDisplayableUrl(servletRequest, new HashMap<String, Object>()));
            assertTrue(descriptor2.getLink().getDisplayableUrl(servletRequest, new HashMap<String, Object>()).startsWith(BASE_URL + "/my/addon"));
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
    public void velocityKiller() throws Exception
    {
        WebItemModuleBean bean = newWebItemBean()
                .withName(new I18nProperty(VELOCITY_LABEL, ""))
                .withKey(MODULE_KEY)
                .withUrl("/my/addon")
                .withLocation("system.top.navigation.bar")
                .withTooltip(new I18nProperty(VELOCITY_TOOLTIP,""))
                .build();

        ConnectAddonBean addon = newConnectAddonBean()
                .withName("JD Plugin")
                .withKey(pluginKey)
                .withBaseurl(BASE_URL)
                .withAuthentication(AuthenticationBean.none())
                .withModules("webItems", bean)
                .build();

        Plugin plugin = null;

        try
        {
            plugin = testPluginInstaller.installAddon(addon);

            WebItemModuleDescriptor descriptor = (WebItemModuleDescriptor) getConnectPlugin().getModuleDescriptor(addonAndModuleKey(pluginKey,MODULE_KEY));

            Map<String,Object> vars = new HashMap<String, Object>();
            vars.put("var","ooops");
            vars.put("awesome","awesome-ooops");
            
            String label = descriptor.getWebLabel().getDisplayableLabel(servletRequest,vars);
            String tooltip = descriptor.getTooltip().getDisplayableLabel(servletRequest,vars);
            
            //by the time we get the displayable label, it's already gone through velocity and so we get the literal variables non-escaped.
            assertEquals(VELOCITY_LABEL, label);
            assertEquals(VELOCITY_TOOLTIP, descriptor.getTooltip().getDisplayableLabel(servletRequest,vars));

        }
        finally
        {
            if (null != plugin)
            {
                testPluginInstaller.uninstallAddon(plugin);
            }
        }
    }
    
    private Plugin getConnectPlugin()
    {
        return pluginAccessor.getPlugin(ConnectPluginInfo.getPluginKey());
    }
}
