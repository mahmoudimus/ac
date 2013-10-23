package com.atlassian.plugin.connect.plugin.capabilities.provider;

import com.atlassian.plugin.connect.plugin.capabilities.beans.AbstractConnectTabPanelCapabilityBean;
import com.atlassian.plugin.connect.plugin.capabilities.beans.nested.I18nProperty;
import com.atlassian.plugin.connect.plugin.capabilities.descriptor.ConnectVersionTabPanelModuleDescriptor;
import com.atlassian.plugin.connect.plugin.capabilities.descriptor.ConnectVersionTabPanelModuleDescriptorFactory;

import static com.atlassian.plugin.connect.plugin.capabilities.beans.ConnectVersionTabPanelCapabilityBean.newVersionTabPanelBean;

public class ConnectVersionTabPanelModuleProviderTest extends AbstractConnectTabPanelModuleProviderTest<ConnectVersionTabPanelModuleDescriptorFactory>
{
    public ConnectVersionTabPanelModuleProviderTest()
    {
        super(ConnectVersionTabPanelModuleDescriptor.class, ConnectVersionTabPanelModuleDescriptorFactory.class);
    }

    @Override
    protected AbstractConnectTabPanelCapabilityBean createCapabilityBean()
    {
        return newVersionTabPanelBean()
                .withName(new I18nProperty(ADDON_NAME, ADDON_I18_NAME_KEY))
                .withKey(ADDON_KEY)
                .withUrl(ADDON_URL)
                .withWeight(99)
                .build();
    }

    protected ConnectVersionTabPanelModuleProvider createProvider() {
        ConnectVersionTabPanelModuleProvider provider = new ConnectVersionTabPanelModuleProvider(moduleDescriptorFactory);
        return provider;
    }

}
