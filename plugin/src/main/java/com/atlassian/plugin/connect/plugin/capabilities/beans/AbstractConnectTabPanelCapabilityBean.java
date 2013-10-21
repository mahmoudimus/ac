package com.atlassian.plugin.connect.plugin.capabilities.beans;

import com.atlassian.plugin.connect.plugin.capabilities.beans.builder.NameToKeyBeanBuilder;

/**
 * Base class for TabPanel capability beans
 */
public abstract class AbstractConnectTabPanelCapabilityBean extends NameToKeyBean
{
    private String url;
    private Integer weight;

    public AbstractConnectTabPanelCapabilityBean() {
        this("", 100);
    }

    public AbstractConnectTabPanelCapabilityBean(String url, Integer weight)
    {
        this.url = url;
        this.weight = weight;
    }

    public AbstractConnectTabPanelCapabilityBean(NameToKeyBeanBuilder builder)
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
}
