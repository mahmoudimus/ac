package it.com.atlassian.plugin.connect.descriptor;

import java.io.IOException;

import com.atlassian.plugin.ModuleDescriptor;
import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.connect.modules.beans.AuthenticationType;
import com.atlassian.plugin.connect.modules.beans.ConnectAddonBean;
import com.atlassian.plugin.connect.modules.beans.WebSectionModuleBean;
import com.atlassian.plugin.connect.modules.beans.builder.SingleConditionBeanBuilder;
import com.atlassian.plugin.connect.modules.beans.nested.I18nProperty;
import com.atlassian.plugin.connect.plugin.capabilities.descriptor.ConnectWebSectionModuleDescriptorFactory;
import com.atlassian.plugin.connect.plugin.capabilities.provider.ConnectTabPanelModuleProvider;
import com.atlassian.plugin.connect.test.plugin.capabilities.testobjects.PluginForTests;
import com.atlassian.plugin.connect.testsupport.TestPluginInstaller;
import com.atlassian.plugin.web.conditions.AndCompositeCondition;
import com.atlassian.plugin.web.descriptors.WebSectionModuleDescriptor;
import com.atlassian.plugins.osgi.test.AtlassianPluginsTestRunner;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import it.com.atlassian.plugin.connect.TestAuthenticator;

import static com.atlassian.plugin.connect.modules.beans.AuthenticationBean.newAuthenticationBean;
import static com.atlassian.plugin.connect.modules.beans.ConnectAddonBean.newConnectAddonBean;
import static com.atlassian.plugin.connect.modules.beans.WebSectionModuleBean.newWebSectionBean;
import static com.atlassian.plugin.connect.test.plugin.capabilities.beans.matchers.ConditionMatchers.isCompositeConditionContaining;
import static com.atlassian.plugin.connect.test.plugin.capabilities.beans.matchers.ConditionMatchers.isCompositeConditionContainingSimpleName;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

@RunWith(AtlassianPluginsTestRunner.class)
public class WebSectionDescriptorFactoryTest
{
    public static final String PLUGIN_KEY = "my-awesome-plugin";
    public static final String PLUGIN_NAME = "My Plugin™";
    public static final String MODULE_NAME = "My Web Section";
    public static final String MODULE_NAME_KEY = "my.websection";
    public static final String MODULE_KEY = "my-web-section";
    public static final String MODULE_COMPLETE_KEY = PLUGIN_KEY + ":" + MODULE_KEY;
    public static final String LOCATION = "com.atlassian.jira.plugin.headernav.left.context";
    public static final int WEIGHT = 50;
    
    private final ConnectWebSectionModuleDescriptorFactory descriptorFactory;
    protected final TestPluginInstaller testPluginInstaller;
    protected final TestAuthenticator testAuthenticator;
    
    private Plugin plugin;
    private Plugin installedPlugin;
    private WebSectionModuleBean bean;
    private WebSectionModuleDescriptor descriptor;

    public WebSectionDescriptorFactoryTest(ConnectWebSectionModuleDescriptorFactory descriptorFactory, TestPluginInstaller testPluginInstaller, TestAuthenticator testAuthenticator)
    {
        this.descriptorFactory = descriptorFactory;
        this.testPluginInstaller = testPluginInstaller;
        this.testAuthenticator = testAuthenticator;
    }

    @Before
    public void setup()
    {
        this.plugin = new PluginForTests(PLUGIN_KEY, PLUGIN_NAME);
        this.bean = createBean();

        this.descriptor = descriptorFactory.createModuleDescriptor(plugin, bean);
    }

    @After
    public void destroy()
    {
        if(null != installedPlugin)
        {
            try
            {
                testPluginInstaller.uninstallPlugin(installedPlugin);
                installedPlugin = null;
            }
            catch (IOException e)
            {
                installedPlugin = null;
            }

        }
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

        ConnectAddonBean addonBean = newConnectAddonBean()
                .withBaseurl(testPluginInstaller.getInternalAddonBaseUrl(PLUGIN_KEY))
                .withKey(PLUGIN_KEY)
                .withName(PLUGIN_NAME)
                .withAuthentication(newAuthenticationBean().withType(AuthenticationType.NONE).build())
                .withModule("webSections",bean)
                .build();

        installedPlugin = testPluginInstaller.installPlugin(addonBean);

        return (WebSectionModuleDescriptor) installedPlugin.getModuleDescriptor(MODULE_KEY);
    }

    @Test
    public void keyIsCorrect() throws Exception
    {
        assertThat(descriptor.getKey(), is(MODULE_KEY));
    }

    @Test
    public void completeKeyIsCorrect() throws Exception
    {
        assertThat(descriptor.getCompleteKey(), is(MODULE_COMPLETE_KEY));
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

    @Test
    public void nameIsCorrect() throws IOException
    {
        //note, we need to use an actual installed plugin so i18n props are loaded
        WebSectionModuleDescriptor liveDescriptor = getDescriptorFromInstalledPlugin();
        assertThat(liveDescriptor.getName(), is(MODULE_NAME));
    }

    @Test
    public void conditionIsCorrect() throws IOException
    {
        //note, we need to use an actual installed plugin so conditions are loaded properly
        WebSectionModuleDescriptor liveDescriptor = getDescriptorFromInstalledPlugin();
        assertThat(liveDescriptor.getCondition(), isCompositeConditionContainingSimpleName(AndCompositeCondition.class, "UserLoggedInCondition"));
    }
}
