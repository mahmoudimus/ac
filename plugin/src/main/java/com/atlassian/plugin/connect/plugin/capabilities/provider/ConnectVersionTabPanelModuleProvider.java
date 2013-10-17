package com.atlassian.plugin.connect.plugin.capabilities.provider;

import com.atlassian.plugin.connect.plugin.capabilities.beans.ConnectVersionTabPanelCapabilityBean;
import com.atlassian.plugin.connect.plugin.capabilities.descriptor.ConnectVersionTabPanelModuleDescriptorFactory;
import com.atlassian.plugin.connect.plugin.capabilities.descriptor.RelativeAddOnUrlConverter;

import static com.atlassian.plugin.connect.plugin.capabilities.beans.ConnectVersionTabPanelCapabilityBean.newVersionTabPageBean;

//@Component
public class ConnectVersionTabPanelModuleProvider extends AbstractConnectTabPanelModuleProvider<ConnectVersionTabPanelCapabilityBean, ConnectVersionTabPanelModuleDescriptorFactory>
{

    //    @Autowired
    public ConnectVersionTabPanelModuleProvider(ConnectVersionTabPanelModuleDescriptorFactory issueTabFactory, RelativeAddOnUrlConverter relativeAddOnUrlConverter)
    {
        super(issueTabFactory, relativeAddOnUrlConverter);
    }

    @Override
    protected ConnectVersionTabPanelCapabilityBean createCapabilityBean(ConnectVersionTabPanelCapabilityBean bean, String localUrl)
    {
        return newVersionTabPageBean(bean).withUrl(localUrl).build();
    }

}
