package com.atlassian.plugin.connect.plugin.capabilities.descriptor;

import com.atlassian.jira.plugin.projectpanel.ProjectTabPanel;
import com.atlassian.plugin.connect.plugin.capabilities.beans.AbstractConnectTabPanelCapabilityBean;
import com.atlassian.plugin.connect.plugin.capabilities.beans.ConnectProjectTabPanelCapabilityBean;
import com.atlassian.plugin.connect.plugin.capabilities.beans.nested.I18nProperty;
import com.atlassian.plugin.connect.plugin.capabilities.util.ConnectAutowireUtil;


public class ConnectProjectTabPanelModuleDescriptorFactoryTest extends AbstractConnectTabPanelModuleDescriptorFactoryTest
{
    public ConnectProjectTabPanelModuleDescriptorFactoryTest()
    {
        super(ConnectProjectTabPanelModuleDescriptor.class, ProjectTabPanel.class);
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