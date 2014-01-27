package it.com.atlassian.plugin.connect.provider.confluence;

import java.security.Principal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import com.atlassian.confluence.pages.Page;
import com.atlassian.confluence.plugin.descriptor.web.WebInterfaceContext;
import com.atlassian.confluence.spaces.Space;
import com.atlassian.jira.project.Project;
import com.atlassian.plugin.ModuleDescriptor;
import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.connect.modules.beans.AddOnUrlContext;
import com.atlassian.plugin.connect.modules.beans.ConnectAddonBean;
import com.atlassian.plugin.connect.modules.beans.WebItemModuleBean;
import com.atlassian.plugin.connect.modules.beans.nested.I18nProperty;
import com.atlassian.plugin.connect.plugin.capabilities.provider.WebItemModuleProvider;
import com.atlassian.plugin.web.descriptors.WebItemModuleDescriptor;
import com.atlassian.plugins.osgi.test.AtlassianPluginsTestRunner;
import com.atlassian.sal.api.auth.AuthenticationListener;


import com.atlassian.sal.api.auth.Authenticator;
import com.atlassian.sal.api.user.UserManager;
import com.atlassian.sal.api.web.context.HttpContext;

import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

import it.com.atlassian.plugin.connect.TestPluginInstaller;

import static com.atlassian.plugin.connect.modules.beans.ConnectAddonBean.newConnectAddonBean;
import static com.atlassian.plugin.connect.modules.beans.WebItemModuleBean.newWebItemBean;
import static com.google.common.collect.Lists.newArrayList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(AtlassianPluginsTestRunner.class)
public class ConfluenceWebItemModuleProviderTest
{
    public static final String CONTEXT_PATH = "http://ondemand.com/jira";
    public static final String PLUGIN_KEY = "my-plugin";
    public static final String PLUGIN_NAME = "My Plugin";
    public static final String MODULE_NAME = "My Web Item";
    public static final String MODULE_KEY = "my-web-item";
    public static final String BASE_URL = "http://my.connect.addon.com";
    public static final String SPACE_KEY = "TS";

    private final WebItemModuleProvider webItemModuleProvider;
    private final TestPluginInstaller testPluginInstaller;
    private final AuthenticationListener authenticationListener;
    private final UserManager userManager;
    private final HttpContext httpContext;
    private HttpServletRequest servletRequest;

    public ConfluenceWebItemModuleProviderTest(WebItemModuleProvider webItemModuleProvider, TestPluginInstaller testPluginInstaller, AuthenticationListener authenticationListener, UserManager userManager, HttpContext httpContext)
    {
        this.webItemModuleProvider = webItemModuleProvider;
        this.testPluginInstaller = testPluginInstaller;
        this.authenticationListener = authenticationListener;
        this.userManager = userManager;
        this.httpContext = httpContext;
    }

    @BeforeClass
    public void setup()
    {
        this.servletRequest = mock(HttpServletRequest.class);
        when(servletRequest.getContextPath()).thenReturn(CONTEXT_PATH);
        
        Principal principal = userManager.resolve("admin");
        authenticationListener.authenticationSuccess(new Authenticator.Result.Success(principal),httpContext.getRequest(),httpContext.getResponse());
    }

    @Test
    public void singleAddonLinkWithReplacement() throws Exception
    {
        System.out.println("session user: " + userManager.getRemoteUser().getUsername());
        WebItemModuleBean bean = newWebItemBean()
                .withName(new I18nProperty(MODULE_NAME, ""))
                .withKey(MODULE_KEY)
                .withUrl("/my/addon?mySpace={space.key}")
                .withLocation("atl.admin/menu")
                .withContext(AddOnUrlContext.addon)
                .build();

        ConnectAddonBean addon = newConnectAddonBean()
                .withName(PLUGIN_NAME)
                .withKey(PLUGIN_KEY)
                .withBaseurl(BASE_URL)
                .withModules("webItems",bean)
                .build();

        Plugin plugin = null;

        try
        {
            plugin = testPluginInstaller.installPlugin(addon);

            List<ModuleDescriptor> descriptors = webItemModuleProvider.provideModules(plugin, "webItems", newArrayList(bean));

            assertEquals(1, descriptors.size());

            WebItemModuleDescriptor descriptor = (WebItemModuleDescriptor) descriptors.get(0);
            descriptor.enabled();

            Map<String, Object> context = new HashMap<String, Object>();
            Page page = mock(Page.class);
            Space space = mock(Space.class);
            WebInterfaceContext wic = mock(WebInterfaceContext.class);

            when(space.getId()).thenReturn(1234L);
            when(space.getKey()).thenReturn(SPACE_KEY);

            context.put("webInterfaceContext",wic);

            String convertedUrl = descriptor.getLink().getDisplayableUrl(servletRequest, context);

            assertTrue("wrong url prefix. expected: " + BASE_URL + "/my/addon but got: " + convertedUrl,convertedUrl.startsWith(BASE_URL + "/my/addon"));
            assertTrue("space key not found in: " + convertedUrl, convertedUrl.contains("mySpace=" + SPACE_KEY));
        }
        finally
        {
            if(null != plugin)
            {
                testPluginInstaller.uninstallPlugin(plugin);
            }
        }
    }
}
