package com.atlassian.plugin.connect.plugin.capabilities.beans;

import com.atlassian.plugin.connect.plugin.capabilities.beans.builder.NameToKeyBeanBuilder;

import static com.google.common.base.Preconditions.checkNotNull;

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
        this.url = checkNotNull(url);
        this.weight = checkNotNull(weight);
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
