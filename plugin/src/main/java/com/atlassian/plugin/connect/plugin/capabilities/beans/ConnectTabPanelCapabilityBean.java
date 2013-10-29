package com.atlassian.plugin.connect.plugin.capabilities.beans;

import com.atlassian.plugin.ModuleDescriptor;
import com.atlassian.plugin.connect.plugin.capabilities.beans.builder.ConnectTabPanelCapabilityBeanBuilder;

import static com.google.common.base.Preconditions.checkNotNull;

public class ConnectTabPanelCapabilityBean extends BeanWithKeyAndParamsAndConditions
{
    private String url;
    private Integer weight;
    
    private transient TabPanelDescriptorHints descriptorHints;

    public ConnectTabPanelCapabilityBean() {
        this("", 100, new TabPanelDescriptorHints());
    }

    public ConnectTabPanelCapabilityBean(String url, Integer weight, TabPanelDescriptorHints descriptorHints)
    {
        this.url = checkNotNull(url);
        this.weight = checkNotNull(weight);
        this.descriptorHints = checkNotNull(descriptorHints);
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

        if (null == descriptorHints)
        {
            this.descriptorHints = new TabPanelDescriptorHints();
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

    public TabPanelDescriptorHints getDescriptorHints()
    {
        return descriptorHints;
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
