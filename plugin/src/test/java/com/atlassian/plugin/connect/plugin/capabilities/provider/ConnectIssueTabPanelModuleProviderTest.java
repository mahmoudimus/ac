package com.atlassian.plugin.connect.plugin.capabilities.provider;

import com.atlassian.plugin.connect.plugin.capabilities.beans.AbstractConnectTabPanelCapabilityBean;
import com.atlassian.plugin.connect.plugin.capabilities.beans.nested.I18nProperty;
import com.atlassian.plugin.connect.plugin.capabilities.descriptor.ConnectIssueTabPanelModuleDescriptor;
import com.atlassian.plugin.connect.plugin.capabilities.descriptor.ConnectIssueTabPanelModuleDescriptorFactory;

import static com.atlassian.plugin.connect.plugin.capabilities.beans.ConnectIssueTabPanelCapabilityBean.newIssueTabPanelBean;

public class ConnectIssueTabPanelModuleProviderTest extends AbstractConnectTabPanelModuleProviderTest<ConnectIssueTabPanelModuleDescriptorFactory>
{
    public ConnectIssueTabPanelModuleProviderTest()
    {
        super(ConnectIssueTabPanelModuleDescriptor.class, ConnectIssueTabPanelModuleDescriptorFactory.class);
    }

    @Override
    protected AbstractConnectTabPanelCapabilityBean createCapabilityBean()
    {
        return newIssueTabPanelBean()
                .withName(new I18nProperty(ADDON_NAME, ADDON_I18_NAME_KEY))
                .withKey(ADDON_KEY)
                .withUrl(ADDON_URL)
                .withWeight(99)
                .build();
    }

    protected ConnectIssueTabPanelModuleProvider createProvider() {
        ConnectIssueTabPanelModuleProvider provider = new ConnectIssueTabPanelModuleProvider(moduleDescriptorFactory);
        return provider;
    }

}
