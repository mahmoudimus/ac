package it.com.atlassian.plugin.connect.jira;

import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.PluginAccessor;
import com.atlassian.plugin.connect.modules.beans.AuthenticationType;
import com.atlassian.plugin.connect.modules.beans.ConnectAddonBean;
import com.atlassian.plugin.connect.modules.beans.WebSectionModuleBean;
import com.atlassian.plugin.connect.modules.beans.builder.SingleConditionBeanBuilder;
import com.atlassian.plugin.connect.modules.beans.nested.I18nProperty;
import com.atlassian.plugin.connect.plugin.ConnectPluginInfo;
import com.atlassian.plugin.connect.plugin.capabilities.descriptor.ConnectWebSectionModuleDescriptorFactory;
import com.atlassian.plugin.connect.plugin.capabilities.provider.DefaultConnectModuleProviderContext;
import com.atlassian.plugin.connect.util.AddonUtil;
import com.atlassian.plugin.connect.testsupport.TestPluginInstaller;
import com.atlassian.plugin.web.Condition;
import com.atlassian.plugin.web.conditions.AndCompositeCondition;
import com.atlassian.plugin.web.descriptors.WebSectionModuleDescriptor;
import com.atlassian.plugins.osgi.test.AtlassianPluginsTestRunner;
import com.atlassian.plugin.connect.util.auth.TestAuthenticator;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.HashMap;

import static com.atlassian.plugin.connect.modules.beans.AuthenticationBean.newAuthenticationBean;
import static com.atlassian.plugin.connect.modules.beans.ConnectAddonBean.newConnectAddonBean;
import static com.atlassian.plugin.connect.modules.beans.WebSectionModuleBean.newWebSectionBean;
import static com.atlassian.plugin.connect.modules.util.ModuleKeyUtils.addonAndModuleKey;
import static com.atlassian.plugin.connect.plugin.capabilities.beans.matchers.ConditionMatchers.isCompositeConditionContainingSimpleName;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.mock;

@RunWith(AtlassianPluginsTestRunner.class)
public class WebSectionDescriptorFactoryTest
{
    public static final String PLUGIN_NAME = "My Plugin™";
    public static final String MODULE_NAME = "My Web Section";
    public static final String MODULE_NAME_KEY = "my.websection";
    public static final String MODULE_KEY = "my-web-section";
    public static final String LOCATION = "com.atlassian.jira.plugin.headernav.left.context";
    public static final int WEIGHT = 50;
    
    private final ConnectWebSectionModuleDescriptorFactory descriptorFactory;
    protected final TestPluginInstaller testPluginInstaller;
    protected final TestAuthenticator testAuthenticator;
    private final PluginAccessor pluginAccessor;
    
    private Plugin installedPlugin;
    private WebSectionModuleBean bean;
    private WebSectionModuleDescriptor descriptor;

    private String pluginKey;
    private ConnectAddonBean addonBean;

    public WebSectionDescriptorFactoryTest(ConnectWebSectionModuleDescriptorFactory descriptorFactory, TestPluginInstaller testPluginInstaller, TestAuthenticator testAuthenticator, PluginAccessor pluginAccessor)
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
        addonBean = createAddonBean();
        

        this.descriptor = descriptorFactory.createModuleDescriptor(new DefaultConnectModuleProviderContext(addonBean),
                getConnectPlugin(), bean);
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

        addonBean = null;
    }

    private WebSectionModuleBean createBean()
    {
        return newWebSectionBean()
                .withName(new I18nProperty(MODULE_NAME, MODULE_NAME_KEY))
                .withKey(MODULE_KEY)
                .withLocation(LOCATION)
                .withWeight(WEIGHT)
                .withConditions(new SingleConditionBeanBuilder().withCondition("user_is_logged_in").build())
                .build();
    }

    protected WebSectionModuleDescriptor getDescriptorFromInstalledPlugin() throws IOException
    {
        testAuthenticator.authenticateUser("admin");

        ConnectAddonBean addonBean = createAddonBean();

        installedPlugin = testPluginInstaller.installAddon(addonBean);

        return (WebSectionModuleDescriptor) getConnectPlugin().getModuleDescriptor(addonAndModuleKey(pluginKey,MODULE_KEY));
    }

    protected ConnectAddonBean createAddonBean()
    {
        return newConnectAddonBean()
                .withBaseurl(testPluginInstaller.getInternalAddonBaseUrl(pluginKey))
                .withKey(pluginKey)
                .withName(PLUGIN_NAME)
                .withAuthentication(newAuthenticationBean().withType(AuthenticationType.NONE).build())
                .withModule("webSections",createBean())
                .build();
    }

    @Test
    public void keyIsCorrect() throws Exception
    {
        assertThat(descriptor.getKey(), is(addonAndModuleKey(pluginKey, MODULE_KEY)));
    }

    @Test
    public void completeKeyIsCorrect() throws Exception
    {
        assertThat(descriptor.getCompleteKey(), is(ConnectPluginInfo.getPluginKey() + ":" + addonAndModuleKey(pluginKey,MODULE_KEY)));
    }

    @Test
    public void locationIsCorrect()
    {
        assertThat(descriptor.getLocation(), is(LOCATION));
    }

    @Test
    public void weightIsCorrect()
    {
        assertThat(descriptor.getWeight(), is(50));
    }

    @Test
    public void i18nKeyIsCorrect()
    {
        assertThat(descriptor.getI18nNameKey(), is(MODULE_NAME_KEY));
    }

    @Ignore("I18n names won't work in confluence until we get them to change the way they load resource bundles")
    @Test
    public void nameIsCorrect() throws IOException
    {
        //note, we need to use an actual installed plugin so i18n props are loaded
        WebSectionModuleDescriptor liveDescriptor = getDescriptorFromInstalledPlugin();
        String label = liveDescriptor.getWebLabel().getDisplayableLabel(mock(HttpServletRequest.class),new HashMap<String, Object>());
        assertThat(label, is(MODULE_NAME));
    }

    @Ignore("stupid hamcrest matchers. this fails due to hamcrest even though the actual and excpected match.")
    @Test
    public void conditionIsCorrect() throws IOException
    {
        //note, we need to use an actual installed plugin so conditions are loaded properly
        WebSectionModuleDescriptor liveDescriptor = getDescriptorFromInstalledPlugin();
        
        Condition condition = liveDescriptor.getCondition();
        assertThat(condition, isCompositeConditionContainingSimpleName(AndCompositeCondition.class, "UserLoggedInCondition"));
    }

    protected Plugin getConnectPlugin()
    {
        return pluginAccessor.getPlugin(ConnectPluginInfo.getPluginKey());
    }
}
