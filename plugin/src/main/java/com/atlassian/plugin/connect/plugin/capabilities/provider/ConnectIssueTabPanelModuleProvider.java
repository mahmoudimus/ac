package com.atlassian.plugin.connect.plugin.capabilities.provider;

import com.atlassian.plugin.connect.plugin.capabilities.beans.ConnectIssueTabPanelCapabilityBean;
import com.atlassian.plugin.connect.plugin.capabilities.descriptor.ConnectIssueTabPanelModuleDescriptorFactory;

//@Component
public class ConnectIssueTabPanelModuleProvider extends AbstractConnectTabPanelModuleProvider<ConnectIssueTabPanelCapabilityBean, ConnectIssueTabPanelModuleDescriptorFactory>
{

    //    @Autowired
    public ConnectIssueTabPanelModuleProvider(ConnectIssueTabPanelModuleDescriptorFactory issueTabFactory)
    {
        super(issueTabFactory);
    }

    @Override
    protected ConnectIssueTabPanelCapabilityBean createCapabilityBean(ConnectIssueTabPanelCapabilityBean bean)
    {
        return ConnectIssueTabPanelCapabilityBean.newIssueTabPanelBean(bean).build();
    }

}
