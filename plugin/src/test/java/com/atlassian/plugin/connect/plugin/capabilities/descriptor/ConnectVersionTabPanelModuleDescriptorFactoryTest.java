package com.atlassian.plugin.connect.plugin.capabilities.descriptor;

import com.atlassian.jira.plugin.versionpanel.VersionTabPanel;
import com.atlassian.plugin.connect.plugin.capabilities.beans.AbstractConnectTabPanelCapabilityBean;
import com.atlassian.plugin.connect.plugin.capabilities.beans.ConnectVersionTabPanelCapabilityBean;
import com.atlassian.plugin.connect.plugin.capabilities.beans.nested.I18nProperty;
import com.atlassian.plugin.connect.plugin.capabilities.util.ConnectAutowireUtil;


public class ConnectVersionTabPanelModuleDescriptorFactoryTest extends AbstractConnectTabPanelModuleDescriptorFactoryTest
{
    public ConnectVersionTabPanelModuleDescriptorFactoryTest()
    {
        super(ConnectVersionTabPanelModuleDescriptor.class, VersionTabPanel.class);
    }

    @Override
    protected AbstractConnectTabPanelModuleDescriptorFactory createDescriptorFactory(ConnectAutowireUtil connectAutowireUtil)
    {
        return new ConnectVersionTabPanelModuleDescriptorFactory(connectAutowireUtil);
    }

    @Override
    protected AbstractConnectTabPanelCapabilityBean createCapabilityBean()
    {
        return ConnectVersionTabPanelCapabilityBean.newVersionTabPanelBean()
            .withName(new I18nProperty("My Tab Page", "my.tabpage"))
            .withUrl("http://www.google.com")
            .withWeight(99)
            .build();
    }
}
