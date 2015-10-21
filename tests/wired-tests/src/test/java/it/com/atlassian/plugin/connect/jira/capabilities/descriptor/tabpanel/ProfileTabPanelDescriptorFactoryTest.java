package it.com.atlassian.plugin.connect.jira.capabilities.descriptor.tabpanel;

import com.atlassian.plugin.PluginAccessor;
import com.atlassian.plugin.connect.jira.capabilities.descriptor.tabpanel.ConnectTabPanelModuleDescriptorFactory;
import com.atlassian.plugin.connect.jira.capabilities.descriptor.tabpanel.ConnectViewProfilePanelModuleDescriptor;
import com.atlassian.plugin.connect.jira.capabilities.provider.ProfileTabPanelModuleProvider;
import com.atlassian.plugin.connect.jira.iframe.tabpanel.TabPanelDescriptorHints;
import com.atlassian.plugin.connect.modules.beans.ProfileTabPanelModuleMeta;
import com.atlassian.plugin.connect.testsupport.TestPluginInstaller;
import com.atlassian.plugin.connect.testsupport.util.auth.TestAuthenticator;
import com.atlassian.plugins.osgi.test.Application;
import com.atlassian.plugins.osgi.test.AtlassianPluginsTestRunner;
import org.junit.Test;
import org.junit.runner.RunWith;

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
    protected String getModuleFieldName()
    {
        return new ProfileTabPanelModuleMeta().getDescriptorKey();
    }

    @Override
    protected TabPanelDescriptorHints getDescriptorHints()
    {
        return ProfileTabPanelModuleProvider.HINTS;
    }

    @Test
    public void createsElementWithCorrectOrder()
    {
        assertEquals(MODULE_WEIGHT, ((ConnectViewProfilePanelModuleDescriptor) getDescriptor()).getOrder());
    }

    @Test
    public void createsElementWithCorrectClass()
    {
        assertEquals(getDescriptorHints().getModuleClass().getName(), ((ConnectViewProfilePanelModuleDescriptor) getDescriptor()).getModuleClassName());
    }
}
