package it.com.atlassian.plugin.connect.provider.jira;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import com.atlassian.jira.project.Project;
import com.atlassian.plugin.ModuleDescriptor;
import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.connect.modules.beans.AddOnUrlContext;
import com.atlassian.plugin.connect.modules.beans.AuthenticationBean;
import com.atlassian.plugin.connect.modules.beans.ConnectAddonBean;
import com.atlassian.plugin.connect.modules.beans.WebItemModuleBean;
import com.atlassian.plugin.connect.modules.beans.nested.I18nProperty;
import com.atlassian.plugin.connect.plugin.capabilities.provider.DefaultConnectModuleProviderContext;
import com.atlassian.plugin.connect.plugin.capabilities.provider.WebItemModuleProvider;
import com.atlassian.plugin.connect.testsupport.TestPluginInstaller;
import com.atlassian.plugin.web.descriptors.WebItemModuleDescriptor;
import com.atlassian.plugins.osgi.test.Application;
import com.atlassian.plugins.osgi.test.AtlassianPluginsTestRunner;

import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

import it.com.atlassian.plugin.connect.TestAuthenticator;

import static com.atlassian.plugin.connect.modules.beans.ConnectAddonBean.newConnectAddonBean;
import static com.atlassian.plugin.connect.modules.beans.WebItemModuleBean.newWebItemBean;
import static com.google.common.collect.Lists.newArrayList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@Application("jira")
@RunWith(AtlassianPluginsTestRunner.class)
public class JiraWebitemModuleProviderTest
{
    public static final String CONTEXT_PATH = "http://ondemand.com/jira";
    public static final String PLUGIN_KEY = "my-plugin";
    public static final String PLUGIN_NAME = "My Plugin";
    public static final String MODULE_NAME = "My Web Item";
    public static final String MODULE_KEY = "my-web-item";
    public static final String BASE_URL = "http://my.connect.addon.com";
    public static final String PROJECT_KEY = "TEST";
    public static final Long PROJECT_ID = 1234L;
    
    private final WebItemModuleProvider webItemModuleProvider;
    private final TestPluginInstaller testPluginInstaller;
    private final TestAuthenticator testAuthenticator;
    private HttpServletRequest servletRequest;
    private ConnectAddonBean addon;

    public JiraWebitemModuleProviderTest(WebItemModuleProvider webItemModuleProvider, TestPluginInstaller testPluginInstaller, TestAuthenticator testAuthenticator)
    {
        this.webItemModuleProvider = webItemModuleProvider;
        this.testPluginInstaller = testPluginInstaller;
        this.testAuthenticator = testAuthenticator;
    }

    @BeforeClass
    public void setup()
    {
        this.addon = newConnectAddonBean().withKey("my-plugin").build();
        this.servletRequest = mock(HttpServletRequest.class);
        when(servletRequest.getContextPath()).thenReturn(CONTEXT_PATH);
        
        testAuthenticator.authenticateUser("admin");
    }

    @Test
    public void singleAddonLinkWithReplacement() throws Exception
    {
        WebItemModuleBean bean = newWebItemBean()
                .withName(new I18nProperty(MODULE_NAME, ""))
                .withKey(MODULE_KEY)
                .withUrl("/my/addon?myProject={project.key}")
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

            List<ModuleDescriptor> descriptors = webItemModuleProvider.provideModules(
                    new DefaultConnectModuleProviderContext(addon), plugin, "webItems", newArrayList(bean));

            assertEquals(1, descriptors.size());

            WebItemModuleDescriptor descriptor = (WebItemModuleDescriptor) descriptors.get(0);
            descriptor.enabled();

            Map<String, Object> context = new HashMap<String, Object>();
            Project project = mock(Project.class);
            when(project.getKey()).thenReturn(PROJECT_KEY);
            when(project.getId()).thenReturn(PROJECT_ID);
            
            context.put("project",project);
            
            String convertedUrl = descriptor.getLink().getDisplayableUrl(servletRequest, context);
            
            assertTrue("wrong url prefix. expected: " + BASE_URL + "/my/addon but got: " + convertedUrl,convertedUrl.startsWith(BASE_URL + "/my/addon"));
            assertTrue("project key not found in: " + convertedUrl, convertedUrl.contains("myProject=" + PROJECT_KEY));
        }
        finally
        {
            if(null != plugin)
            {
                testPluginInstaller.uninstallAddon(plugin);
            }
        }
    }
}
