package it.com.atlassian.plugin.connect.provider;

import java.util.HashMap;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import com.atlassian.plugin.ModuleDescriptor;
import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.connect.modules.beans.WebItemModuleBean;
import com.atlassian.plugin.connect.modules.beans.nested.I18nProperty;
import com.atlassian.plugin.connect.plugin.capabilities.provider.WebItemModuleProvider;
import com.atlassian.plugin.connect.plugin.capabilities.testobjects.PluginForTests;
import com.atlassian.plugin.web.descriptors.WebItemModuleDescriptor;
import com.atlassian.plugins.osgi.test.AtlassianPluginsTestRunner;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.osgi.framework.BundleContext;

import static com.atlassian.plugin.connect.modules.beans.WebItemModuleBean.newWebItemBean;
import static com.google.common.collect.Lists.newArrayList;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;

@RunWith(AtlassianPluginsTestRunner.class)
public class WebItemModuleProviderTest
{
    public static final String PLUGIN_KEY = "my-plugin";
    public static final String PLUGIN_NAME = "My Plugin";
    public static final String MODULE_NAME = "My Web Item";
    public static final String MODULE_KEY = "my-web-item";

    private final WebItemModuleProvider webItemModuleProvider;
    private final BundleContext bundleContext;

    public WebItemModuleProviderTest(WebItemModuleProvider webItemModuleProvider, BundleContext bundleContext) 
    {
        this.webItemModuleProvider = webItemModuleProvider;
        this.bundleContext = bundleContext;
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
        List<ModuleDescriptor> descriptors = webItemModuleProvider.provideModules(plugin, "webItems",newArrayList(bean));

        assertEquals(1, descriptors.size());

        WebItemModuleDescriptor descriptor = (WebItemModuleDescriptor) descriptors.get(0);
        descriptor.enabled();

        assertEquals("http://www.google.com", descriptor.getLink().getDisplayableUrl(mock(HttpServletRequest.class), new HashMap<String, Object>()));
    }
}
