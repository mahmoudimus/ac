package it.com.atlassian.plugin.connect.descriptor.jira;

import com.atlassian.plugin.PluginAccessor;
import com.atlassian.plugin.connect.jira.capabilities.descriptor.tabpanel.ConnectIssueTabPanelModuleDescriptor;
import com.atlassian.plugin.connect.jira.capabilities.descriptor.tabpanel.ConnectTabPanelModuleDescriptorFactory;
import com.atlassian.plugin.connect.jira.capabilities.provider.ConnectTabPanelModuleProvider;
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
public class IssueTabPanelDescriptorFactoryTest extends AbstractTabPanelDescriptorFactoryTest
{
    public IssueTabPanelDescriptorFactoryTest(ConnectTabPanelModuleDescriptorFactory descriptorFactory, TestPluginInstaller testPluginInstaller, TestAuthenticator testAuthenticator, PluginAccessor pluginAccessor)
    {
        super(descriptorFactory, testPluginInstaller, testAuthenticator, pluginAccessor);
    }

    @Override
    protected TabPanelDescriptorHints getDescriptorHints()
    {
        return ConnectTabPanelModuleProvider.FIELD_TO_HINTS.get(ConnectTabPanelModuleProvider.ISSUE_TAB_PANELS);
    }

    @Test
    @Override
    public void createsElementWithCorrectOrder()
    {
        assertEquals(MODULE_WEIGHT, ((ConnectIssueTabPanelModuleDescriptor) getDescriptor()).getOrder());
    }

    @Test
    @Override
    public void createsElementWithCorrectClass()
    {
        assertEquals(getDescriptorHints().getModuleClass().getName(), ((ConnectIssueTabPanelModuleDescriptor) getDescriptor()).getModuleClassName());
    }

    @Test
    public void createsElementWithCorrectLabelText() throws IOException
    {
        assertEquals(MODULE_NAME, ((ConnectIssueTabPanelModuleDescriptor) getDescriptorFromInstalledPlugin()).getLabel());
    }
}
