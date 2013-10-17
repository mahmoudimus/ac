package com.atlassian.plugin.connect.plugin.capabilities.provider;

import com.atlassian.plugin.connect.plugin.capabilities.beans.ConnectIssueTabPanelCapabilityBean;
import com.atlassian.plugin.connect.plugin.capabilities.descriptor.ConnectIssueTabPanelModuleDescriptorFactory;
import com.atlassian.plugin.connect.plugin.capabilities.descriptor.RelativeAddOnUrlConverter;

import static com.atlassian.plugin.connect.plugin.capabilities.beans.ConnectIssueTabPanelCapabilityBean.newIssueTabPageBean;

//@Component
public class ConnectIssueTabPanelModuleProvider extends AbstractConnectTabPanelModuleProvider<ConnectIssueTabPanelCapabilityBean, ConnectIssueTabPanelModuleDescriptorFactory>
{

    //    @Autowired
    public ConnectIssueTabPanelModuleProvider(ConnectIssueTabPanelModuleDescriptorFactory issueTabFactory, RelativeAddOnUrlConverter relativeAddOnUrlConverter)
    {
        super(issueTabFactory, relativeAddOnUrlConverter);
    }

    @Override
    protected ConnectIssueTabPanelCapabilityBean createCapabilityBean(ConnectIssueTabPanelCapabilityBean bean, String localUrl)
    {
        return newIssueTabPageBean(bean).withUrl(localUrl).build();
    }

}
