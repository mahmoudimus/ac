package it.com.atlassian.plugin.connect.jira.capabilities.descriptor.tabpanel;

import com.atlassian.plugin.PluginAccessor;
import com.atlassian.plugin.connect.jira.capabilities.descriptor.tabpanel.ConnectIssueTabPanelModuleDescriptor;
import com.atlassian.plugin.connect.jira.capabilities.descriptor.tabpanel.ConnectTabPanelModuleDescriptorFactory;
import com.atlassian.plugin.connect.jira.capabilities.provider.IssueTabPanelModuleProvider;
import com.atlassian.plugin.connect.jira.iframe.tabpanel.TabPanelDescriptorHints;
import com.atlassian.plugin.connect.modules.beans.IssueTabPanelModuleMeta;
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
public class IssueTabPanelDescriptorFactoryTest extends AbstractTabPanelDescriptorFactoryTest
{
    public IssueTabPanelDescriptorFactoryTest(ConnectTabPanelModuleDescriptorFactory descriptorFactory, TestPluginInstaller testPluginInstaller, TestAuthenticator testAuthenticator, PluginAccessor pluginAccessor)
    {
        super(descriptorFactory, testPluginInstaller, testAuthenticator, pluginAccessor);
    }

    @Override
    protected String getModuleFieldName()
    {
        return new IssueTabPanelModuleMeta().getDescriptorKey();
    }

    @Override
    protected TabPanelDescriptorHints getDescriptorHints()
    {
        return IssueTabPanelModuleProvider.HINTS;
    }

    @Test
    public void createsElementWithCorrectOrder()
    {
        assertEquals(MODULE_WEIGHT, ((ConnectIssueTabPanelModuleDescriptor) getDescriptor()).getOrder());
    }

    @Test
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
