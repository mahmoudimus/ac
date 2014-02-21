package it.com.atlassian.plugin.connect.descriptor.tabpanel.jira;

import java.io.IOException;

import com.atlassian.plugin.connect.plugin.capabilities.descriptor.tabpanel.ConnectComponentTabPanelModuleDescriptor;
import com.atlassian.plugin.connect.plugin.capabilities.descriptor.tabpanel.ConnectTabPanelModuleDescriptorFactory;
import com.atlassian.plugin.connect.plugin.capabilities.provider.ConnectTabPanelModuleProvider;
import com.atlassian.plugin.connect.plugin.capabilities.provider.TabPanelDescriptorHints;
import com.atlassian.plugin.connect.testsupport.TestPluginInstaller;
import com.atlassian.plugins.osgi.test.AtlassianPluginsTestRunner;

import org.junit.Test;
import org.junit.runner.RunWith;

import it.com.atlassian.plugin.connect.TestAuthenticator;

import static org.junit.Assert.assertEquals;

@RunWith(AtlassianPluginsTestRunner.class)
public class ComponentTabPanelDescriptorFactoryTest extends AbstractTabPanelDescriptorFactoryTest
{
    public ComponentTabPanelDescriptorFactoryTest(ConnectTabPanelModuleDescriptorFactory descriptorFactory, TestPluginInstaller testPluginInstaller, TestAuthenticator testAuthenticator)
    {
        super(descriptorFactory, testPluginInstaller, testAuthenticator);
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
