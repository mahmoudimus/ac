package com.atlassian.plugin.connect.plugin.capabilities.provider;

import com.atlassian.plugin.connect.plugin.capabilities.beans.AbstractConnectTabPanelCapabilityBean;
import com.atlassian.plugin.connect.plugin.capabilities.beans.nested.I18nProperty;
import com.atlassian.plugin.connect.plugin.capabilities.descriptor.ConnectComponentTabPanelModuleDescriptor;
import com.atlassian.plugin.connect.plugin.capabilities.descriptor.ConnectComponentTabPanelModuleDescriptorFactory;

import static com.atlassian.plugin.connect.plugin.capabilities.beans.ConnectComponentTabPanelCapabilityBean.newComponentTabPanelBean;

public class ConnectComponentTabPanelModuleProviderTest extends AbstractConnectTabPanelModuleProviderTest<ConnectComponentTabPanelModuleDescriptorFactory>
{
    public ConnectComponentTabPanelModuleProviderTest()
    {
        super(ConnectComponentTabPanelModuleDescriptor.class, ConnectComponentTabPanelModuleDescriptorFactory.class);
    }

    @Override
    protected AbstractConnectTabPanelCapabilityBean createCapabilityBean()
    {
        return newComponentTabPanelBean()
                .withName(new I18nProperty(ADDON_NAME, ADDON_I18_NAME_KEY))
                .withKey(ADDON_KEY)
                .withUrl(ADDON_URL)
                .withWeight(99)
                .build();
    }

    protected ConnectComponentTabPanelModuleProvider createProvider() {
        ConnectComponentTabPanelModuleProvider provider = new ConnectComponentTabPanelModuleProvider(moduleDescriptorFactory);
        return provider;
    }

}
