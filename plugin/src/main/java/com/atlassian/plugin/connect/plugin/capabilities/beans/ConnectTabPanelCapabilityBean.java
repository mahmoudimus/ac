package com.atlassian.plugin.connect.plugin.capabilities.beans;

import com.atlassian.plugin.connect.plugin.capabilities.beans.builder.ConnectTabPanelCapabilityBeanBuilder;

import static com.google.common.base.Preconditions.checkNotNull;

public class ConnectTabPanelCapabilityBean extends BeanWithKeyAndParamsAndConditions
{
    private String url;
    private Integer weight;

    public ConnectTabPanelCapabilityBean() {
        this("", 100, new TabPanelDescriptorHints());
    }

    public ConnectTabPanelCapabilityBean(String url, Integer weight, TabPanelDescriptorHints descriptorHints)
    {
        this.url = checkNotNull(url);
        this.weight = checkNotNull(weight);
    }

    public ConnectTabPanelCapabilityBean(ConnectTabPanelCapabilityBeanBuilder builder)
    {
        super(builder);
        
        if(null == weight)
        {
            this.weight = 100;
        }
        if (null == url)
        {
            this.url = "";
        }
    }

    public String getUrl()
    {
        return url;
    }

    public int getWeight()
    {
        return weight;
    }

    public static ConnectTabPanelCapabilityBeanBuilder newTabPanelBean()
    {
        return new ConnectTabPanelCapabilityBeanBuilder();
    }

    public static ConnectTabPanelCapabilityBeanBuilder newTabPanelBean(ConnectTabPanelCapabilityBean defaultBean)
    {
        return new ConnectTabPanelCapabilityBeanBuilder(defaultBean);
    }
}
