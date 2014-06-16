package it.com.atlassian.plugin.connect.descriptor.jira;

import com.atlassian.plugin.PluginAccessor;
import com.atlassian.plugin.connect.plugin.capabilities.descriptor.tabpanel.ConnectComponentTabPanelModuleDescriptor;
import com.atlassian.plugin.connect.plugin.capabilities.descriptor.tabpanel.ConnectTabPanelModuleDescriptorFactory;
import com.atlassian.plugin.connect.plugin.capabilities.provider.ConnectTabPanelModuleProvider;
import com.atlassian.plugin.connect.plugin.capabilities.provider.TabPanelDescriptorHints;
import com.atlassian.plugin.connect.testsupport.TestPluginInstaller;
import com.atlassian.plugins.osgi.test.Application;
import com.atlassian.plugins.osgi.test.AtlassianPluginsTestRunner;
import it.com.atlassian.plugin.connect.TestAuthenticator;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.IOException;

import static org.junit.Assert.assertEquals;

@Application("jira")
@RunWith(AtlassianPluginsTestRunner.class)
public class ComponentTabPanelDescriptorFactoryTest extends AbstractTabPanelDescriptorFactoryTest
{
    public ComponentTabPanelDescriptorFactoryTest(ConnectTabPanelModuleDescriptorFactory descriptorFactory, TestPluginInstaller testPluginInstaller, TestAuthenticator testAuthenticator, PluginAccessor pluginAccessor)
    {
        super(descriptorFactory, testPluginInstaller, testAuthenticator, pluginAccessor);
    }

    @Override
    protected TabPanelDescriptorHints getDescriptorHints()
    {
        return ConnectTabPanelModuleProvider.FIELD_TO_HINTS.get(ConnectTabPanelModuleProvider.COMPONENT_TAB_PANELS);
    }

    @Test
    @Override
    public void createsElementWithCorrectOrder()
    {
        assertEquals(MODULE_WEIGHT, ((ConnectComponentTabPanelModuleDescriptor) getDescriptor()).getOrder());
    }

    @Test
    @Override
    public void createsElementWithCorrectClass()
    {
        assertEquals(getDescriptorHints().getModuleClass().getName(), ((ConnectComponentTabPanelModuleDescriptor) getDescriptor()).getModuleClassName());
    }

    @Test
    public void createsElementWithCorrectLabelText() throws IOException
    {
        assertEquals(MODULE_NAME, ((ConnectComponentTabPanelModuleDescriptor) getDescriptorFromInstalledPlugin()).getLabel());
    }

    @Test
    public void createsElementWithCorrectLabelKey()
    {
        assertEquals(MODULE_I18N, ((ConnectComponentTabPanelModuleDescriptor) getDescriptor()).getLabelKey());
    }
}