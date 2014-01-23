package it.com.atlassian.plugin.connect.provider;

import java.util.HashMap;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import com.atlassian.plugin.ModuleDescriptor;
import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.connect.modules.beans.AddOnUrlContext;
import com.atlassian.plugin.connect.modules.beans.WebItemModuleBean;
import com.atlassian.plugin.connect.modules.beans.nested.I18nProperty;
import com.atlassian.plugin.connect.plugin.capabilities.provider.WebItemModuleProvider;
import com.atlassian.plugin.connect.test.plugin.capabilities.ConnectAsserts;
import com.atlassian.plugin.connect.test.plugin.capabilities.testobjects.PluginForTests;
import com.atlassian.plugin.web.descriptors.WebItemModuleDescriptor;
import com.atlassian.plugins.osgi.test.AtlassianPluginsTestRunner;

import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

import static com.atlassian.plugin.connect.modules.beans.WebItemModuleBean.newWebItemBean;
import static com.google.common.collect.Lists.newArrayList;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(AtlassianPluginsTestRunner.class)
public class WebItemModuleProviderTest
{
    public static final String PLUGIN_KEY = "my-plugin";
    public static final String PLUGIN_NAME = "My Plugin";
    public static final String MODULE_NAME = "My Web Item";
    public static final String MODULE_KEY = "my-web-item";
    public static final String OTHER_MODULE_NAME = "My Other Web Item";
    public static final String OTHER_MODULE_KEY = "my-other-web-item";
    public static final String CONTEXT_PATH = "http://ondemand.com/someProduct";

    private final WebItemModuleProvider webItemModuleProvider;
    private HttpServletRequest servletRequest;

    public WebItemModuleProviderTest(WebItemModuleProvider webItemModuleProvider)
    {
        this.webItemModuleProvider = webItemModuleProvider;
    }
    
    @BeforeClass
    public void setup()
    {
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

        Plugin plugin = new PluginForTests(PLUGIN_KEY, PLUGIN_NAME);
        List<ModuleDescriptor> descriptors = webItemModuleProvider.provideModules(plugin, "webItems", newArrayList(bean));

        assertEquals(1, descriptors.size());

        WebItemModuleDescriptor descriptor = (WebItemModuleDescriptor) descriptors.get(0);
        descriptor.enabled();

        assertEquals("http://www.google.com", descriptor.getLink().getDisplayableUrl(servletRequest, new HashMap<String, Object>()));
    }

    @Test
    public void singleAbsoluteLinkWithAddOnContext() throws Exception
    {
        WebItemModuleBean bean = newWebItemBean()
                .withName(new I18nProperty(MODULE_NAME, ""))
                .withKey(MODULE_KEY)
                .withUrl("http://www.google.com")
                .withLocation("atl.admin/menu")
                .withContext(AddOnUrlContext.page)
                .build();

        Plugin plugin = new PluginForTests(PLUGIN_KEY, PLUGIN_NAME);
        List<ModuleDescriptor> descriptors = webItemModuleProvider.provideModules(plugin, "webItems", newArrayList(bean));

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

        Plugin plugin = new PluginForTests(PLUGIN_KEY, PLUGIN_NAME);
        List<ModuleDescriptor> descriptors = webItemModuleProvider.provideModules(plugin, "webItems", newArrayList(bean));

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

        Plugin plugin = new PluginForTests(PLUGIN_KEY, PLUGIN_NAME);
        List<ModuleDescriptor> descriptors = webItemModuleProvider.provideModules(plugin, "webItems", newArrayList(bean));

        assertEquals(1, descriptors.size());

        WebItemModuleDescriptor descriptor = (WebItemModuleDescriptor) descriptors.get(0);
        descriptor.enabled();

        assertEquals(CONTEXT_PATH + "/plugins/servlet/ac/my-plugin/some-page-key", descriptor.getLink().getDisplayableUrl(servletRequest, new HashMap<String, Object>()));
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

        Plugin plugin = new PluginForTests(PLUGIN_KEY, PLUGIN_NAME);
        List<ModuleDescriptor> descriptors = webItemModuleProvider.provideModules(plugin, "webItems", newArrayList(bean));

        assertEquals(1, descriptors.size());

        WebItemModuleDescriptor descriptor = (WebItemModuleDescriptor) descriptors.get(0);
        descriptor.enabled();

        assertEquals(CONTEXT_PATH + "/local/jira/admin", descriptor.getLink().getDisplayableUrl(servletRequest, new HashMap<String, Object>()));
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

        Plugin plugin = new PluginForTests(PLUGIN_KEY, PLUGIN_NAME);
        List<ModuleDescriptor> descriptors = webItemModuleProvider.provideModules(plugin, "webItems", newArrayList(bean, bean2));

        assertEquals(2, descriptors.size());

        WebItemModuleDescriptor descriptor = (WebItemModuleDescriptor) descriptors.get(0);
        descriptor.enabled();

        WebItemModuleDescriptor descriptor2 = (WebItemModuleDescriptor) descriptors.get(1);
        descriptor2.enabled();

        assertEquals("http://www.google.com", descriptor.getLink().getDisplayableUrl(servletRequest, new HashMap<String, Object>()));
        assertEquals(CONTEXT_PATH + "/plugins/servlet/ac/my-key/my/addon", descriptor2.getLink().getDisplayableUrl(servletRequest, new HashMap<String, Object>()));
    }
}
