package com.atlassian.plugin.connect.plugin.capabilities.provider;

import com.atlassian.plugin.connect.plugin.capabilities.beans.ConnectComponentTabPanelCapabilityBean;
import com.atlassian.plugin.connect.plugin.capabilities.descriptor.ConnectComponentTabPanelModuleDescriptorFactory;

import static com.atlassian.plugin.connect.plugin.capabilities.beans.ConnectComponentTabPanelCapabilityBean.newComponentTabPanelBean;

/**
 * Module Provider for a Connect ComponentTabPanel Module
 */
public class ConnectComponentTabPanelModuleProvider extends AbstractConnectTabPanelModuleProvider<ConnectComponentTabPanelCapabilityBean, ConnectComponentTabPanelModuleDescriptorFactory>
{
    public ConnectComponentTabPanelModuleProvider(ConnectComponentTabPanelModuleDescriptorFactory issueTabFactory)
    {
        super(issueTabFactory);
    }

    @Override
    protected ConnectComponentTabPanelCapabilityBean createCapabilityBean(ConnectComponentTabPanelCapabilityBean bean)
    {
        return newComponentTabPanelBean(bean).build();
    }

}
