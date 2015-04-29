package it.com.atlassian.plugin.connect.jira.capabilities.descriptor.tabpanel;

import com.atlassian.plugin.PluginAccessor;
import com.atlassian.plugin.connect.jira.capabilities.descriptor.tabpanel.ConnectProjectTabPanelModuleDescriptor;
import com.atlassian.plugin.connect.jira.capabilities.descriptor.tabpanel.ConnectTabPanelModuleDescriptorFactory;
import com.atlassian.plugin.connect.jira.capabilities.provider.ConnectTabPanelModuleProvider;
import com.atlassian.plugin.connect.jira.iframe.tabpanel.TabPanelDescriptorHints;
import com.atlassian.plugin.connect.testsupport.TestPluginInstaller;
import com.atlassian.plugins.osgi.test.Application;
import com.atlassian.plugins.osgi.test.AtlassianPluginsTestRunner;
import com.atlassian.plugin.connect.util.auth.TestAuthenticator;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.IOException;

import static org.junit.Assert.assertEquals;

@Application("jira")
@RunWith(AtlassianPluginsTestRunner.class)
public class ProjectTabPanelDescriptoryFactoryTest extends AbstractTabPanelDescriptorFactoryTest
{
    public ProjectTabPanelDescriptoryFactoryTest(ConnectTabPanelModuleDescriptorFactory descriptorFactory, TestPluginInstaller testPluginInstaller, TestAuthenticator testAuthenticator, PluginAccessor pluginAccessor)
    {
        super(descriptorFactory, testPluginInstaller, testAuthenticator, pluginAccessor);
    }

    @Override
    protected TabPanelDescriptorHints getDescriptorHints()
    {
        return ConnectTabPanelModuleProvider.FIELD_TO_HINTS.get(ConnectTabPanelModuleProvider.PROJECT_TAB_PANELS);
    }

    @Test
    @Override
    public void createsElementWithCorrectOrder()
    {
        assertEquals(MODULE_WEIGHT, ((ConnectProjectTabPanelModuleDescriptor) getDescriptor()).getOrder());
    }

    @Test
    @Override
    public void createsElementWithCorrectClass()
    {
        assertEquals(getDescriptorHints().getModuleClass().getName(), ((ConnectProjectTabPanelModuleDescriptor) getDescriptor()).getModuleClassName());
    }

    @Test
    public void createsElementWithCorrectLabelText() throws IOException
    {
        assertEquals(MODULE_NAME, ((ConnectProjectTabPanelModuleDescriptor) getDescriptorFromInstalledPlugin()).getLabel());
    }

    @Test
    public void createsElementWithCorrectLabelKey()
    {
        assertEquals(MODULE_I18N, ((ConnectProjectTabPanelModuleDescriptor) getDescriptor()).getLabelKey());
    }
}
