package it.com.atlassian.plugin.connect.descriptor.jira;

import com.atlassian.plugin.PluginAccessor;
import com.atlassian.plugin.connect.plugin.capabilities.descriptor.tabpanel.ConnectTabPanelModuleDescriptorFactory;
import com.atlassian.plugin.connect.plugin.capabilities.descriptor.tabpanel.ConnectViewProfilePanelModuleDescriptor;
import com.atlassian.plugin.connect.plugin.capabilities.provider.ConnectTabPanelModuleProvider;
import com.atlassian.plugin.connect.plugin.capabilities.provider.TabPanelDescriptorHints;
import com.atlassian.plugin.connect.testsupport.TestPluginInstaller;
import com.atlassian.plugins.osgi.test.Application;
import com.atlassian.plugins.osgi.test.AtlassianPluginsTestRunner;

import org.junit.Test;
import org.junit.runner.RunWith;

import it.com.atlassian.plugin.connect.TestAuthenticator;

import static org.junit.Assert.assertEquals;

@Application("jira")
@RunWith(AtlassianPluginsTestRunner.class)
public class ProfileTabPanelDescriptorFactoryTest extends AbstractTabPanelDescriptorFactoryTest
{
    public ProfileTabPanelDescriptorFactoryTest(ConnectTabPanelModuleDescriptorFactory descriptorFactory, TestPluginInstaller testPluginInstaller, TestAuthenticator testAuthenticator, PluginAccessor pluginAccessor)
    {
        super(descriptorFactory, testPluginInstaller, testAuthenticator, pluginAccessor);
    }

    @Override
    protected TabPanelDescriptorHints getDescriptorHints()
    {
        return ConnectTabPanelModuleProvider.FIELD_TO_HINTS.get(ConnectTabPanelModuleProvider.PROFILE_TAB_PANELS);
    }

    @Test
    @Override
    public void createsElementWithCorrectOrder()
    {
        assertEquals(MODULE_WEIGHT, ((ConnectViewProfilePanelModuleDescriptor) getDescriptor()).getOrder());
    }

    @Test
    @Override
    public void createsElementWithCorrectClass()
    {
        assertEquals(getDescriptorHints().getModuleClass().getName(), ((ConnectViewProfilePanelModuleDescriptor) getDescriptor()).getModuleClassName());
    }
}
