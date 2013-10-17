package com.atlassian.plugin.connect.plugin.capabilities.beans.builder;

import com.atlassian.plugin.connect.plugin.capabilities.beans.AbstractConnectTabPanelCapabilityBean;

public class AbstractConnectTabPanelCapabilityBeanBuilder<T extends AbstractConnectTabPanelCapabilityBeanBuilder, B extends AbstractConnectTabPanelCapabilityBean>
        extends NameToKeyBeanBuilder<T, B>
{
    private String url;
    private int weight;

    public AbstractConnectTabPanelCapabilityBeanBuilder(AbstractConnectTabPanelCapabilityBean defaultBean)
    {
        super(defaultBean);
        this.weight = defaultBean.getWeight();
        this.url = defaultBean.getUrl();
    }

    public AbstractConnectTabPanelCapabilityBeanBuilder()
    {
    }

    public AbstractConnectTabPanelCapabilityBeanBuilder withUrl(String link)
    {
        this.url = link;
        return this;
    }

    public AbstractConnectTabPanelCapabilityBeanBuilder<T, B> withWeight(int weight)
    {
        this.weight = weight;
        return this;
    }

    public String getUrl()
    {
        return url;
    }

    public int getWeight()
    {
        return weight;
    }

}
