package it.com.atlassian.plugin.connect.jira.capabilities.descriptor.tabpanel;

import com.atlassian.plugin.PluginAccessor;
import com.atlassian.plugin.connect.jira.capabilities.descriptor.tabpanel.ConnectProjectTabPanelModuleDescriptor;
import com.atlassian.plugin.connect.jira.capabilities.descriptor.tabpanel.ConnectTabPanelModuleDescriptorFactory;
import com.atlassian.plugin.connect.jira.capabilities.provider.ProjectTabPanelModuleProvider;
import com.atlassian.plugin.connect.jira.iframe.tabpanel.TabPanelDescriptorHints;
import com.atlassian.plugin.connect.modules.beans.ProjectTabPanelModuleMeta;
import com.atlassian.plugin.connect.testsupport.TestPluginInstaller;
import com.atlassian.plugin.connect.testsupport.util.auth.TestAuthenticator;
import com.atlassian.plugins.osgi.test.Application;
import com.atlassian.plugins.osgi.test.AtlassianPluginsTestRunner;
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
    protected String getModuleFieldName()
    {
        return new ProjectTabPanelModuleMeta().getDescriptorKey();
    }

    @Override
    protected TabPanelDescriptorHints getDescriptorHints()
    {
        return ProjectTabPanelModuleProvider.HINTS;
    }

    @Test
    public void createsElementWithCorrectOrder()
    {
        assertEquals(MODULE_WEIGHT, ((ConnectProjectTabPanelModuleDescriptor) getDescriptor()).getOrder());
    }

    @Test
    public void createsElementWithCorrectClass()
    {
        assertEquals(getDescriptorHints().getModuleClass().getName(), ((ConnectProjectTabPanelModuleDescriptor) getDescriptor()).getModuleClassName());
    }

    @Test
    public void createsElementWithCorrectLabelText() throws IOException
    {
        assertEquals(MODULE_NAME, ((ConnectProjectTabPanelModuleDescriptor) getDescriptorFromInstalledPlugin()).getLabel());
    }
}
