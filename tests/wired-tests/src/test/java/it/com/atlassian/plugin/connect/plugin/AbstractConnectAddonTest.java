package it.com.atlassian.plugin.connect.plugin;

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
import com.atlassian.plugin.connect.testsupport.util.auth.TestAuthenticator;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.List;

import static com.atlassian.plugin.connect.modules.beans.ConnectAddonBean.newConnectAddonBean;
import static com.atlassian.plugin.connect.modules.beans.WebItemModuleBean.newWebItemBean;
import static com.google.common.collect.Lists.newArrayList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Abstract class for all tests that register some connect add-ons and then assert some things about them
 */
public abstract class AbstractConnectAddonTest
{
    public static final String CONTEXT_PATH = "http://ondemand.com/";
    public static final String PLUGIN_KEY = "my-plugin";
    public static final String PLUGIN_NAME = "My Plugin";
    public static final String MODULE_NAME = "My Web Item";
    public static final String MODULE_KEY = "my-web-item";
    public static final String BASE_URL = "http://my.connect.addon.com";
    public static final String ADDON_PATH = "/my/addon";

    private final WebItemModuleProvider webItemModuleProvider;
    private final TestPluginInstaller testPluginInstaller;
    private final TestAuthenticator testAuthenticator;
    protected HttpServletRequest servletRequest;
    private Plugin plugin;

    public AbstractConnectAddonTest(WebItemModuleProvider webItemModuleProvider, TestPluginInstaller testPluginInstaller, TestAuthenticator testAuthenticator)
    {
        this.webItemModuleProvider = webItemModuleProvider;
        this.testPluginInstaller = testPluginInstaller;
        this.testAuthenticator = testAuthenticator;
    }

    @BeforeClass
    public void setup()
    {
        this.servletRequest = mock(HttpServletRequest.class);
        when(servletRequest.getContextPath()).thenReturn(CONTEXT_PATH);
    }

    @Before
    public void setupTest()
    {
        testAuthenticator.authenticateUser("admin");
    }

    @After
    public void cleanUp() throws IOException
    {
        if (plugin != null)
        {
            testPluginInstaller.uninstallAddon(plugin);
            plugin = null;
        }
    }

    protected final void actAsAnonymous()
    {
        testAuthenticator.unauthenticate();
    }

    protected final WebItemModuleDescriptor registerWebItem(String queryString, String location) throws IOException
    {
        checkIfPluginAlreadyInstalled();

        WebItemModuleBean bean = newWebItemBean()
                .withName(new I18nProperty(MODULE_NAME, ""))
                .withKey(MODULE_KEY)
                .withUrl(ADDON_PATH + "?" + queryString)
                .withLocation(location)
                .withContext(AddOnUrlContext.addon)
                .build();

        ConnectAddonBean addon = newConnectAddonBean()
                .withName(PLUGIN_NAME)
                .withKey(PLUGIN_KEY)
                .withBaseurl(BASE_URL)
                .withAuthentication(AuthenticationBean.none())
                .withModules("webItems", bean)
                .build();

        plugin = testPluginInstaller.installAddon(addon);

        List<ModuleDescriptor> descriptors = webItemModuleProvider.createPluginModuleDescriptors(
                newArrayList(bean), new DefaultConnectModuleProviderContext(addon));

        assertEquals(1, descriptors.size());

        WebItemModuleDescriptor descriptor = (WebItemModuleDescriptor) descriptors.get(0);
        descriptor.enabled();

        return descriptor;
    }

    private void checkIfPluginAlreadyInstalled()
    {
        if (plugin != null)
        {
            throw new IllegalStateException("You can test only one add-on at a time!");
        }
    }

    // Equivalent to assertThat(hay, containsString(needle));
    // Hamcrest matchers throw LinkageError for some reason so we need to do this like that.
    protected void assertStringContains(String hay, String needle)
    {
        assertTrue("expected: contains '" + needle + " ', actual: " + hay, hay.contains(needle));
    }
}
