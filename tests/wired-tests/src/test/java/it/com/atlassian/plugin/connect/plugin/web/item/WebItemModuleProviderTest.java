package it.com.atlassian.plugin.connect.plugin.web.item;

import com.atlassian.plugin.ModuleDescriptor;
import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.PluginAccessor;
import com.atlassian.plugin.connect.api.util.ConnectPluginInfo;
import com.atlassian.plugin.connect.api.web.iframe.ConnectIFrameServletPath;
import com.atlassian.plugin.connect.modules.beans.AddonUrlContext;
import com.atlassian.plugin.connect.modules.beans.AuthenticationBean;
import com.atlassian.plugin.connect.modules.beans.ConnectAddonBean;
import com.atlassian.plugin.connect.modules.beans.WebItemModuleBean;
import com.atlassian.plugin.connect.modules.beans.WebItemTargetBean;
import com.atlassian.plugin.connect.modules.beans.WebItemTargetType;
import com.atlassian.plugin.connect.modules.beans.nested.I18nProperty;
import com.atlassian.plugin.connect.modules.beans.nested.dialog.DialogOptions;
import com.atlassian.plugin.connect.plugin.web.item.WebItemModuleProvider;
import com.atlassian.plugin.connect.testsupport.TestPluginInstaller;
import com.atlassian.plugin.connect.testsupport.util.auth.TestAuthenticator;
import com.atlassian.plugin.util.WaitUntil;
import com.atlassian.plugin.web.descriptors.WebItemModuleDescriptor;
import com.atlassian.plugin.web.model.WebLink;
import com.atlassian.plugins.osgi.test.AtlassianPluginsTestRunner;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.servlet.http.HttpServletRequest;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.atlassian.plugin.connect.modules.beans.ConnectAddonBean.newConnectAddonBean;
import static com.atlassian.plugin.connect.modules.beans.WebItemModuleBean.newWebItemBean;
import static com.atlassian.plugin.connect.modules.beans.WebItemTargetBean.newWebItemTargetBean;
import static com.atlassian.plugin.connect.modules.util.ModuleKeyUtils.addonAndModuleKey;
import static com.atlassian.plugin.connect.testsupport.util.AddonUtil.randomPluginKey;
import static com.google.common.collect.Lists.newArrayList;
import static org.hamcrest.MatcherAssert.assertThat;
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
        List<ModuleDescriptor> descriptors = webItemModuleProvider.createPluginModuleDescriptors(newArrayList(bean), addon);

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
                .withContext(AddonUrlContext.page)
                .build();

        Plugin plugin = getConnectPlugin();
        List<ModuleDescriptor> descriptors = webItemModuleProvider.createPluginModuleDescriptors(newArrayList(bean), addon);

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
                .withContext(AddonUrlContext.product)
                .build();

        Plugin plugin = getConnectPlugin();
        List<ModuleDescriptor> descriptors = webItemModuleProvider.createPluginModuleDescriptors(newArrayList(bean), addon);

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
                .withContext(AddonUrlContext.page)
                .build();

        Plugin plugin = getConnectPlugin();
        List<ModuleDescriptor> descriptors = webItemModuleProvider.createPluginModuleDescriptors(newArrayList(bean), addon);

        assertEquals(1, descriptors.size());

        WebItemModuleDescriptor descriptor = (WebItemModuleDescriptor) descriptors.get(0);
        descriptor.enabled();

        String expectedUrl = CONTEXT_PATH + "/plugins/servlet/ac/" + addon.getKey() + "/some-page-key";
        assertThat(descriptor.getLink(), pointsTo(servletRequest, expectedUrl));
    }

    private Matcher<? super WebLink> pointsTo(final HttpServletRequest servletRequest, final String url)
    {
        return new TypeSafeMatcher<WebLink>()
        {
            @Override
            protected boolean matchesSafely(WebLink webLink)
            {
                String displayableUrl = webLink.getDisplayableUrl(servletRequest, Collections.<String, Object>emptyMap());
                return displayableUrl.startsWith(url);
            }

            @Override
            public void describeTo(Description description)
            {
                description.appendText("weblink containing url starting with " + url);
            }
        };
    }

    @Test
    public void singlePageLinkName() throws Exception
    {
        WebItemModuleBean bean = newWebItemBean()
                .withName(new I18nProperty(MODULE_NAME, ""))
                .withKey(MODULE_KEY)
                .withUrl("some-page-key")
                .withLocation("atl.admin/menu")
                .withContext(AddonUrlContext.page)
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
                .withContext(AddonUrlContext.product)
                .build();

        Plugin plugin = getConnectPlugin();
        List<ModuleDescriptor> descriptors = webItemModuleProvider.createPluginModuleDescriptors(newArrayList(bean), addon);

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

            List<ModuleDescriptor> descriptors = webItemModuleProvider.createPluginModuleDescriptors(newArrayList(bean), addon);

            assertEquals(1, descriptors.size());

            WebItemModuleDescriptor descriptor = (WebItemModuleDescriptor) descriptors.get(0);
            descriptor.enabled();

            assertAddonLinkHrefIsCorrect(descriptor, bean, addon);
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
                                .withOptions(DialogOptions.newDialogOptions()
                                                .withWidth("100")
                                                .withHeight("300px")
                                                .build()
                                )
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

            List<ModuleDescriptor> descriptors = webItemModuleProvider.createPluginModuleDescriptors(
                    newArrayList(bean), addon);

            assertEquals(1, descriptors.size());

            WebItemModuleDescriptor descriptor = (WebItemModuleDescriptor) descriptors.get(0);
            descriptor.enabled();

            assertAddonLinkHrefIsCorrect(descriptor, bean, addon);
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

            List<ModuleDescriptor> descriptors = webItemModuleProvider.createPluginModuleDescriptors(newArrayList(bean, bean2), addon);

            assertEquals(2, descriptors.size());

            WebItemModuleDescriptor descriptor = (WebItemModuleDescriptor) descriptors.get(0);
            descriptor.enabled();

            WebItemModuleDescriptor descriptor2 = (WebItemModuleDescriptor) descriptors.get(1);
            descriptor2.enabled();

            assertEquals("http://www.google.com", descriptor.getLink().getDisplayableUrl(servletRequest, new HashMap<String, Object>()));
            assertAddonLinkHrefIsCorrect(descriptor2, bean2, addon);
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

    private void assertAddonLinkHrefIsCorrect(WebItemModuleDescriptor descriptor, WebItemModuleBean webItemModuleBean, ConnectAddonBean addonBean)
    {
        final WebItemTargetBean target = webItemModuleBean.getTarget();
        final String prefix = target.isDialogTarget() || target.isInlineDialogTarget()
                ? ConnectIFrameServletPath.forModule(pluginKey, webItemModuleBean.getKey(addonBean))
                : BASE_URL + "/my/addon";
        final String href = descriptor.getLink().getDisplayableUrl(servletRequest, new HashMap<String, Object>());
        final String message = String.format("Expecting the href to start with '%s' but it was '%s'", prefix, href);
        assertTrue(message, href.startsWith(prefix));
    }

    private Plugin getConnectPlugin()
    {
        return pluginAccessor.getPlugin(ConnectPluginInfo.getPluginKey());
    }
}
