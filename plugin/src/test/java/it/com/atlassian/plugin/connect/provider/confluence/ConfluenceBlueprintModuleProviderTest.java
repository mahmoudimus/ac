package it.com.atlassian.plugin.connect.provider.confluence;

import com.atlassian.confluence.pages.Page;
import com.atlassian.confluence.plugin.descriptor.web.WebInterfaceContext;
import com.atlassian.confluence.spaces.Space;
import com.atlassian.plugin.ModuleDescriptor;
import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.connect.modules.beans.AuthenticationBean;
import com.atlassian.plugin.connect.modules.beans.BlueprintModuleBean;
import com.atlassian.plugin.connect.modules.beans.ConnectAddonBean;
import com.atlassian.plugin.connect.plugin.capabilities.provider.BlueprintModuleProvider;
import com.atlassian.plugin.connect.testsupport.TestPluginInstaller;
import com.atlassian.plugin.web.descriptors.WebItemModuleDescriptor;
import com.atlassian.plugins.osgi.test.Application;
import com.atlassian.plugins.osgi.test.AtlassianPluginsTestRunner;
import it.com.atlassian.plugin.connect.TestAuthenticator;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.atlassian.plugin.connect.modules.beans.BlueprintModuleBean.newWebItemBean;
import static com.atlassian.plugin.connect.modules.beans.ConnectAddonBean.newConnectAddonBean;
import static com.google.common.collect.Lists.newArrayList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@Application("confluence")
@RunWith(AtlassianPluginsTestRunner.class)
public class ConfluenceBlueprintModuleProviderTest {

    public static final String CONTEXT_PATH = "http://ondemand.com/confluence";
    public static final String PLUGIN_KEY = "blueprints-plugin";
    public static final String PLUGIN_NAME = "Blueprints Plugin";
    public static final String MODULE_NAME = "My Blueprint";
    public static final String MODULE_KEY = "my-blueprint";
    public static final String BASE_URL = "http://my.connect.addon.com";
    public static final String SPACE_KEY = "ds";

    private final BlueprintModuleProvider blueprintModuleProvider;
    private final TestPluginInstaller testPluginInstaller;
    private HttpServletRequest servletRequest;
    private final TestAuthenticator testAuthenticator;

    public ConfluenceBlueprintModuleProviderTest(BlueprintModuleProvider blueprintModuleProvider,
                                                 TestPluginInstaller testPluginInstaller,
                                                 TestAuthenticator testAuthenticator) {
        this.blueprintModuleProvider = blueprintModuleProvider;
        this.testPluginInstaller = testPluginInstaller;
        this.testAuthenticator = testAuthenticator;
    }

    @BeforeClass
    public void setup() {
        this.servletRequest = mock(HttpServletRequest.class);
        when(servletRequest.getContextPath()).thenReturn(CONTEXT_PATH);

        testAuthenticator.authenticateUser("admin");

    }

    @Test
    public void singleAddonLinkWithReplacement() throws Exception {
        //System.out.println("session user: " + userManager.getRemoteUser().getUsername());
        BlueprintModuleBean bean = newWebItemBean()
                .withKey(MODULE_KEY)
//                .withUrl("/my/addon?mySpace={space.key}")
                .build();

        ConnectAddonBean addon = newConnectAddonBean()
                .withName(PLUGIN_NAME)
                .withKey(PLUGIN_KEY)
                .withBaseurl(BASE_URL)
                .withAuthentication(AuthenticationBean.none())
                .withModules("blueprints", bean)
                .build();

        Plugin plugin = null;

        try {
            plugin = testPluginInstaller.installAddon(addon);

            List<ModuleDescriptor> descriptors = blueprintModuleProvider.provideModules(addon, plugin, "blueprints", newArrayList(bean));

            // should get a WebItem Descriptor and a Blueprint Descriptor
            assertEquals(2, descriptors.size());

            // check the web item descriptor
            WebItemModuleDescriptor descriptor = (WebItemModuleDescriptor) descriptors.get(0);
            descriptor.enabled();
            Map<String, Object> context = new HashMap<String, Object>();
            Page page = mock(Page.class);
            Space space = mock(Space.class);
            WebInterfaceContext wic = mock(WebInterfaceContext.class);
            when(space.getId()).thenReturn(1234L);
            when(space.getKey()).thenReturn(SPACE_KEY);
            when(wic.getSpace()).thenReturn(space);
            when(wic.getPage()).thenReturn(page);
            context.put("webInterfaceContext", wic);
            String convertedUrl = descriptor.getLink().getDisplayableUrl(servletRequest, context);
            assertTrue("wrong url prefix. expected: " + BASE_URL + "/my/addon but got: " + convertedUrl, convertedUrl.startsWith(BASE_URL + "/my/addon"));
            assertTrue("space key not found in: " + convertedUrl, convertedUrl.contains("mySpace=" + SPACE_KEY));

            // check the blueprint descriptor

        } finally {
            if (null != plugin) {
                testPluginInstaller.uninstallAddon(plugin);
            }
        }
    }


}
