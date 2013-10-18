package com.atlassian.plugin.connect.plugin.capabilities.beans.builder;

import com.atlassian.plugin.connect.plugin.capabilities.beans.AbstractConnectTabPanelCapabilityBean;

public class AbstractConnectTabPanelCapabilityBeanBuilder<T extends AbstractConnectTabPanelCapabilityBeanBuilder, B extends AbstractConnectTabPanelCapabilityBean>
        extends NameToKeyBeanBuilder<T, B>
{
    private String url;
    private Integer weight;

    public AbstractConnectTabPanelCapabilityBeanBuilder(AbstractConnectTabPanelCapabilityBean defaultBean)
    {
        super(defaultBean);
        this.weight = defaultBean.getWeight();
        this.url = defaultBean.getUrl();
    }

    public AbstractConnectTabPanelCapabilityBeanBuilder()
    {
    }

    public T withUrl(String link)
    {
        this.url = link;
        return (T) this;
    }

    public T withWeight(int weight)
    {
        this.weight = weight;
        return (T) this;
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
