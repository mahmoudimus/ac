package com.atlassian.plugin.connect.plugin.capabilities.descriptor;

import com.atlassian.jira.plugin.projectpanel.ProjectTabPanel;
import com.atlassian.plugin.connect.plugin.capabilities.beans.AbstractConnectTabPanelCapabilityBean;
import com.atlassian.plugin.connect.plugin.capabilities.beans.ConnectProjectTabPanelCapabilityBean;
import com.atlassian.plugin.connect.plugin.capabilities.beans.nested.I18nProperty;
import com.atlassian.plugin.connect.plugin.capabilities.util.ConnectAutowireUtil;
import com.atlassian.plugin.connect.plugin.module.jira.projecttab.IFrameProjectTab;

import static com.atlassian.plugin.connect.plugin.capabilities.descriptor.ConnectProjectTabPanelModuleDescriptorFactory.MODULE_PREFIX;


public class ConnectProjectTabPanelModuleDescriptorFactoryTest extends AbstractConnectTabPanelModuleDescriptorFactoryTest
{
    public ConnectProjectTabPanelModuleDescriptorFactoryTest()
    {
        super(ConnectProjectTabPanelModuleDescriptor.class, IFrameProjectTab.class, MODULE_PREFIX);
    }

    @Override
    protected AbstractConnectTabPanelModuleDescriptorFactory createDescriptorFactory(ConnectAutowireUtil connectAutowireUtil)
    {
        return new ConnectProjectTabPanelModuleDescriptorFactory(connectAutowireUtil);
    }

    @Override
    protected AbstractConnectTabPanelCapabilityBean createCapabilityBean(String name, String i18NameKey, String url, int weight)
    {
        return ConnectProjectTabPanelCapabilityBean.newProjectTabPanelBean()
                .withName(new I18nProperty(name, i18NameKey))
                .withUrl(url)
                .withWeight(weight)
                .build();
    }
}