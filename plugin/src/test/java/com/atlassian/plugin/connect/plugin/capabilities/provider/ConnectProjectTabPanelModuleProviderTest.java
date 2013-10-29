package com.atlassian.plugin.connect.plugin.capabilities.provider;

import com.atlassian.plugin.connect.plugin.capabilities.beans.AbstractConnectTabPanelCapabilityBean;
import com.atlassian.plugin.connect.plugin.capabilities.beans.nested.I18nProperty;
import com.atlassian.plugin.connect.plugin.capabilities.descriptor.ConnectProjectTabPanelModuleDescriptor;
import com.atlassian.plugin.connect.plugin.capabilities.descriptor.ConnectProjectTabPanelModuleDescriptorFactory;

import static com.atlassian.plugin.connect.plugin.capabilities.beans.ConnectProjectTabPanelCapabilityBean.newProjectTabPanelBean;

public class ConnectProjectTabPanelModuleProviderTest extends AbstractConnectTabPanelModuleProviderTest<ConnectProjectTabPanelModuleDescriptorFactory>
{
    public ConnectProjectTabPanelModuleProviderTest()
    {
        super(ConnectProjectTabPanelModuleDescriptor.class, ConnectProjectTabPanelModuleDescriptorFactory.class);
    }

    @Override
    protected AbstractConnectTabPanelCapabilityBean createCapabilityBean()
    {
        return newProjectTabPanelBean()
                .withName(new I18nProperty(ADDON_NAME, ADDON_I18_NAME_KEY))
                .withKey(ADDON_KEY)
                .withUrl(ADDON_URL)
                .withWeight(99)
                .build();
    }

    protected ConnectProjectTabPanelModuleProvider createProvider() {
        ConnectProjectTabPanelModuleProvider provider = new ConnectProjectTabPanelModuleProvider(moduleDescriptorFactory);
        return provider;
    }

}
