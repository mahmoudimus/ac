package com.atlassian.plugin.connect.plugin.capabilities.beans.builder;

import com.atlassian.plugin.connect.plugin.capabilities.beans.ConnectIssueTabPanelCapabilityBean;

/**
 * Builder for a ConnectIssueTabPanelCapabilityBean
 */
public class ConnectIssueTabPanelCapabilityBeanBuilder extends NameToKeyBeanBuilder<ConnectIssueTabPanelCapabilityBeanBuilder, ConnectIssueTabPanelCapabilityBean>
{
    private String url;
    private int weight;

    public ConnectIssueTabPanelCapabilityBeanBuilder()
    {

    }

    public ConnectIssueTabPanelCapabilityBeanBuilder(ConnectIssueTabPanelCapabilityBean defaultBean)
    {
        super(defaultBean);

        this.url = defaultBean.getUrl();
        this.weight = defaultBean.getWeight();
    }

    public ConnectIssueTabPanelCapabilityBeanBuilder withUrl(String link)
    {
        this.url = link;
        return this;
    }

    public ConnectIssueTabPanelCapabilityBeanBuilder withWeight(int weight)
    {
        this.weight = weight;
        return this;
    }

    @Override
    public ConnectIssueTabPanelCapabilityBean build()
    {
        return new ConnectIssueTabPanelCapabilityBean(this);
    }
}
