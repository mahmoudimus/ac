package com.atlassian.plugin.connect.plugin.capabilities.descriptor;

import com.atlassian.jira.plugin.componentpanel.ComponentTabPanel;
import com.atlassian.plugin.connect.plugin.capabilities.beans.AbstractConnectTabPanelCapabilityBean;
import com.atlassian.plugin.connect.plugin.capabilities.beans.ConnectComponentTabPanelCapabilityBean;
import com.atlassian.plugin.connect.plugin.capabilities.beans.nested.I18nProperty;
import com.atlassian.plugin.connect.plugin.capabilities.util.ConnectAutowireUtil;


public class ConnectComponentTabPanelModuleDescriptorFactoryTest extends AbstractConnectTabPanelModuleDescriptorFactoryTest
{
    public ConnectComponentTabPanelModuleDescriptorFactoryTest()
    {
        super(ConnectComponentTabPanelModuleDescriptor.class, ComponentTabPanel.class);
    }

    @Override
    protected AbstractConnectTabPanelModuleDescriptorFactory createDescriptorFactory(ConnectAutowireUtil connectAutowireUtil)
    {
        return new ConnectComponentTabPanelModuleDescriptorFactory(connectAutowireUtil);
    }

    @Override
    protected AbstractConnectTabPanelCapabilityBean createCapabilityBean()
    {
        return ConnectComponentTabPanelCapabilityBean.newComponentTabPanelBean()
                .withName(new I18nProperty("My Tab Page", "my.tabpage"))
                .withUrl("http://www.google.com")
                .withWeight(99)
                .build();
    }


}