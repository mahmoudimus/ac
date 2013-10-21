package com.atlassian.plugin.connect.plugin.capabilities.provider;

import com.atlassian.plugin.connect.plugin.capabilities.beans.ConnectVersionTabPanelCapabilityBean;
import com.atlassian.plugin.connect.plugin.capabilities.descriptor.ConnectVersionTabPanelModuleDescriptorFactory;

import static com.atlassian.plugin.connect.plugin.capabilities.beans.ConnectVersionTabPanelCapabilityBean.newVersionTabPanelBean;

/**
 * Module Provider for a Connect VersionTabPanel Module
 */
//@Component
public class ConnectVersionTabPanelModuleProvider extends AbstractConnectTabPanelModuleProvider<ConnectVersionTabPanelCapabilityBean, ConnectVersionTabPanelModuleDescriptorFactory>
{

    //    @Autowired
    public ConnectVersionTabPanelModuleProvider(ConnectVersionTabPanelModuleDescriptorFactory issueTabFactory)
    {
        super(issueTabFactory);
    }

    @Override
    protected ConnectVersionTabPanelCapabilityBean createCapabilityBean(ConnectVersionTabPanelCapabilityBean bean)
    {
        return newVersionTabPanelBean(bean).build();
    }

}
