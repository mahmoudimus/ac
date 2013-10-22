package com.atlassian.plugin.connect.plugin.capabilities.provider;

import com.atlassian.plugin.connect.plugin.capabilities.beans.ConnectProjectTabPanelCapabilityBean;
import com.atlassian.plugin.connect.plugin.capabilities.descriptor.ConnectProjectTabPanelModuleDescriptorFactory;

import static com.atlassian.plugin.connect.plugin.capabilities.beans.ConnectProjectTabPanelCapabilityBean.newProjectTabPanelBean;

/**
 * Module Provider for a Connect ProjectTabPanel Module
 */
public class ConnectProjectTabPanelModuleProvider extends AbstractConnectTabPanelModuleProvider<ConnectProjectTabPanelCapabilityBean, ConnectProjectTabPanelModuleDescriptorFactory>
{
    public ConnectProjectTabPanelModuleProvider(ConnectProjectTabPanelModuleDescriptorFactory issueTabFactory)
    {
        super(issueTabFactory);
    }

    @Override
    protected ConnectProjectTabPanelCapabilityBean createCapabilityBean(ConnectProjectTabPanelCapabilityBean bean)
    {
        return newProjectTabPanelBean(bean).build();
    }

}
