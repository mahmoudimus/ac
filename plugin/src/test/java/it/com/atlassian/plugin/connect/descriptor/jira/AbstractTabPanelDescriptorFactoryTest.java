package it.com.atlassian.plugin.connect.descriptor.jira;

import com.atlassian.plugin.ModuleDescriptor;
import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.PluginAccessor;
import com.atlassian.plugin.connect.modules.beans.AuthenticationType;
import com.atlassian.plugin.connect.modules.beans.ConnectAddonBean;
import com.atlassian.plugin.connect.modules.beans.ConnectTabPanelModuleBean;
import com.atlassian.plugin.connect.modules.beans.nested.I18nProperty;
import com.atlassian.plugin.connect.plugin.ConnectPluginInfo;
import com.atlassian.plugin.connect.plugin.capabilities.descriptor.tabpanel.ConnectTabPanelModuleDescriptorFactory;
import com.atlassian.plugin.connect.plugin.capabilities.provider.ConnectTabPanelModuleProvider;
import com.atlassian.plugin.connect.plugin.capabilities.provider.DefaultConnectModuleProviderContext;
import com.atlassian.plugin.connect.plugin.capabilities.provider.TabPanelDescriptorHints;
import com.atlassian.plugin.connect.test.util.AddonUtil;
import com.atlassian.plugin.connect.testsupport.TestPluginInstaller;
import it.com.atlassian.plugin.connect.TestAuthenticator;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import java.io.IOException;

import static com.atlassian.plugin.connect.modules.beans.AuthenticationBean.newAuthenticationBean;
import static com.atlassian.plugin.connect.modules.beans.ConnectAddonBean.newConnectAddonBean;
import static com.atlassian.plugin.connect.modules.beans.ConnectTabPanelModuleBean.newTabPanelBean;
import static com.atlassian.plugin.connect.modules.util.ModuleKeyUtils.addonAndModuleKey;
import static org.junit.Assert.assertEquals;

public abstract class AbstractTabPanelDescriptorFactoryTest
{
    public static final String PLUGIN_NAME = "Tab Panel Plugin";

    public static final String MODULE_KEY = "my-tab-panel";
    public static final String MODULE_NAME = "My Tab Panel";
    public static final String MODULE_I18N = "my.tab.panel";
    public static final String MODULE_URL = "/myTabPanel";
    public static final int MODULE_WEIGHT = 99;

    private final ConnectTabPanelModuleDescriptorFactory descriptorFactory;
    protected final TestPluginInstaller testPluginInstaller;
    protected final TestAuthenticator testAuthenticator;
    private final PluginAccessor pluginAccessor;

    private TabPanelDescriptorHints descriptorHints;
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
        this.descriptorHints = getDescriptorHints();
        this.bean = createBean();

        this.descriptor = descriptorFactory.createModuleDescriptor(new DefaultConnectModuleProviderContext(createAddonBean()),
                getConnectPlugin(), bean, descriptorHints);
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
                installedPlugin = null;
            }

        }
    }

    protected abstract TabPanelDescriptorHints getDescriptorHints();

    protected ConnectTabPanelModuleBean createBean()
    {
        return newTabPanelBean()
                .withName(new I18nProperty(MODULE_NAME, MODULE_I18N))
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
                .withModule(ConnectTabPanelModuleProvider.DESCRIPTOR_TO_FIELD.get(getDescriptorHints().getDescriptorClass()),
                        bean
                ).build();
    }

    //tests the children need to run due to knowledge of which jira descriptor they inherit from
    public abstract void createsElementWithCorrectOrder();

    public abstract void createsElementWithCorrectClass();

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
