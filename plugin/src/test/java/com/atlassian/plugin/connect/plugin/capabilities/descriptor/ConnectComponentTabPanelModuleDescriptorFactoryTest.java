package com.atlassian.plugin.connect.plugin.capabilities.descriptor;

import com.atlassian.plugin.connect.plugin.capabilities.beans.AbstractConnectTabPanelCapabilityBean;
import com.atlassian.plugin.connect.plugin.capabilities.beans.nested.I18nProperty;
import com.atlassian.plugin.connect.plugin.capabilities.util.ConnectAutowireUtil;
import com.atlassian.plugin.connect.plugin.module.jira.componenttab.IFrameComponentTab;

import static com.atlassian.plugin.connect.plugin.capabilities.beans.ConnectComponentTabPanelCapabilityBean.newComponentTabPanelBean;
import static com.atlassian.plugin.connect.plugin.capabilities.descriptor.ConnectComponentTabPanelModuleDescriptorFactory.MODULE_PREFIX;


public class ConnectComponentTabPanelModuleDescriptorFactoryTest extends AbstractConnectTabPanelModuleDescriptorFactoryTest
{
    public ConnectComponentTabPanelModuleDescriptorFactoryTest()
    {
        super(ConnectComponentTabPanelModuleDescriptor.class, IFrameComponentTab.class, MODULE_PREFIX);
    }

    @Override
    protected AbstractConnectTabPanelModuleDescriptorFactory createDescriptorFactory(ConnectAutowireUtil connectAutowireUtil)
    {
        return new ConnectComponentTabPanelModuleDescriptorFactory(connectAutowireUtil);
    }

    @Override
    protected AbstractConnectTabPanelCapabilityBean createCapabilityBean(String name, String i18NameKey, String url, int weight)
    {
        return newComponentTabPanelBean()
                .withName(new I18nProperty(name, i18NameKey))
                .withUrl(url)
                .withWeight(weight)
                .build();
    }
}