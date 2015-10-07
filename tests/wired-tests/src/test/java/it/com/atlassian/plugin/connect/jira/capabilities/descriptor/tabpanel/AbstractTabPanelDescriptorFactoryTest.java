package it.com.atlassian.plugin.connect.jira.capabilities.descriptor.tabpanel;

import com.atlassian.plugin.ModuleDescriptor;
import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.PluginAccessor;
import com.atlassian.plugin.connect.api.util.ConnectPluginInfo;
import com.atlassian.plugin.connect.jira.capabilities.descriptor.tabpanel.ConnectTabPanelModuleDescriptorFactory;
import com.atlassian.plugin.connect.jira.iframe.tabpanel.TabPanelDescriptorHints;
import com.atlassian.plugin.connect.modules.beans.AuthenticationType;
import com.atlassian.plugin.connect.modules.beans.ConnectAddonBean;
import com.atlassian.plugin.connect.modules.beans.ConnectTabPanelModuleBean;
import com.atlassian.plugin.connect.modules.beans.nested.I18nProperty;
import com.atlassian.plugin.connect.plugin.capabilities.provider.DefaultConnectModuleProviderContext;
import com.atlassian.plugin.connect.testsupport.TestPluginInstaller;
import com.atlassian.plugin.connect.testsupport.util.AddonUtil;
import com.atlassian.plugin.connect.testsupport.util.auth.TestAuthenticator;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

import static com.atlassian.plugin.connect.modules.beans.AuthenticationBean.newAuthenticationBean;
import static com.atlassian.plugin.connect.modules.beans.ConnectAddonBean.newConnectAddonBean;
import static com.atlassian.plugin.connect.modules.beans.ConnectTabPanelModuleBean.newTabPanelBean;
import static com.atlassian.plugin.connect.modules.util.ModuleKeyUtils.addonAndModuleKey;
import static org.junit.Assert.assertEquals;

public abstract class AbstractTabPanelDescriptorFactoryTest
{
    private static final Logger LOG = LoggerFactory.getLogger(AbstractTabPanelDescriptorFactoryTest.class);
    public static final String PLUGIN_NAME = "Tab Panel Plugin";

    public static final String MODULE_KEY = "my-tab-panel";
    public static final String MODULE_NAME = "My Tab Panel";
    public static final String MODULE_URL = "/myTabPanel";
    public static final int MODULE_WEIGHT = 99;

    private final ConnectTabPanelModuleDescriptorFactory descriptorFactory;
    protected final TestPluginInstaller testPluginInstaller;
    protected final TestAuthenticator testAuthenticator;
    private final PluginAccessor pluginAccessor;

    private ConnectTabPanelModuleBean bean;
    private ModuleDescriptor descriptor;
    private Plugin installedPlugin;
    
    private String pluginKey;

    public AbstractTabPanelDescriptorFactoryTest(ConnectTabPanelModuleDescriptorFactory descriptorFactory, TestPluginInstaller testPluginInstaller, TestAuthenticator testAuthenticator, PluginAccessor pluginAccessor)
    {
        this.descriptorFactory = descriptorFactory;
        this.testPluginInstaller = testPluginInstaller;
        this.testAuthenticator = testAuthenticator;
        this.pluginAccessor = pluginAccessor;
    }

    @Before
    public void setup()
    {
        this.pluginKey = AddonUtil.randomPluginKey();
        this.bean = createBean();

        this.descriptor = descriptorFactory.createModuleDescriptor(new DefaultConnectModuleProviderContext(createAddonBean()),
                getConnectPlugin(), bean, getDescriptorHints());
    }
    
    @After
    public void destroy()
    {
        if(null != installedPlugin)
        {
            try
            {
                testPluginInstaller.uninstallAddon(installedPlugin);
                installedPlugin = null;
            }
            catch (IOException e)
            {
                LOG.error("Could not uninstall addon", e);
            }

        }
    }

    protected abstract TabPanelDescriptorHints getDescriptorHints();

    protected ConnectTabPanelModuleBean createBean()
    {
        return newTabPanelBean()
                .withName(new I18nProperty(MODULE_NAME, null))
                .withKey(MODULE_KEY)
                .withUrl(MODULE_URL)
                .withWeight(MODULE_WEIGHT)
                .build();
    }

    protected ModuleDescriptor getDescriptor()
    {
        return descriptor;
    }
    
    protected ModuleDescriptor getDescriptorFromInstalledPlugin() throws IOException
    {
        testAuthenticator.authenticateUser("admin");

        ConnectAddonBean addonBean = createAddonBean();
        
        installedPlugin = testPluginInstaller.installAddon(addonBean);
        
        Plugin connectPlugin = getConnectPlugin();
        return connectPlugin.getModuleDescriptor(addonAndModuleKey(pluginKey,MODULE_KEY));
    }

    protected ConnectAddonBean createAddonBean()
    {
        return newConnectAddonBean()
                .withBaseurl(testPluginInstaller.getInternalAddonBaseUrl(pluginKey))
                .withKey(pluginKey)
                .withName(PLUGIN_NAME)
                .withAuthentication(newAuthenticationBean().withType(AuthenticationType.NONE).build())
                .withModule(getModuleFieldName(), bean)
                .build();
    }

    protected abstract String getModuleFieldName();

    //common tests
    @Test
    public void createsElementWithCorrectKey()
    {
        assertEquals(addonAndModuleKey(pluginKey,MODULE_KEY), descriptor.getKey());
    }

    @Test
    public void createsElementWithCorrectName()
    {
        assertEquals(MODULE_NAME, descriptor.getName());
    }

    @Ignore
    @Test
    public void createsElementWithCorrectUrl()
    {
        //assertEquals(MODULE_URL,((TabPanelModuleDescriptor)descriptor)..get());
        //verify(connectTabPanelModuleDescriptor, times(1)).init(eq(plugin), argThat(hasElementUrl(ADDON_URL)));
    }

    protected Plugin getConnectPlugin()
    {
        return pluginAccessor.getPlugin(ConnectPluginInfo.getPluginKey());
    }
}
