package com.atlassian.plugin.connect.plugin.capabilities.descriptor;

import com.atlassian.jira.plugin.issuetabpanel.IssueTabPanel;
import com.atlassian.plugin.connect.plugin.capabilities.beans.AbstractConnectTabPanelCapabilityBean;
import com.atlassian.plugin.connect.plugin.capabilities.beans.nested.I18nProperty;
import com.atlassian.plugin.connect.plugin.capabilities.util.ConnectAutowireUtil;

import static com.atlassian.plugin.connect.plugin.capabilities.beans.ConnectIssueTabPanelCapabilityBean.newIssueTabPanelBean;

public class ConnectIssueTabPanelModuleDescriptorFactoryTest extends AbstractConnectTabPanelModuleDescriptorFactoryTest
{
    public ConnectIssueTabPanelModuleDescriptorFactoryTest()
    {
        super(ConnectIssueTabPanelModuleDescriptor.class, IssueTabPanel.class);
    }

    @Override
    protected AbstractConnectTabPanelModuleDescriptorFactory createDescriptorFactory(ConnectAutowireUtil connectAutowireUtil)
    {
        return new ConnectIssueTabPanelModuleDescriptorFactory(connectAutowireUtil);
    }

    @Override
    protected AbstractConnectTabPanelCapabilityBean createCapabilityBean()
    {
        return newIssueTabPanelBean()
                .withName(new I18nProperty("My Tab Page", "my.tabpage"))
                .withUrl("http://www.google.com")
                .withWeight(99)
                .build();
    }
}
