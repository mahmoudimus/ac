package com.atlassian.plugin.connect.plugin.capabilities.beans;

import com.atlassian.plugin.connect.plugin.capabilities.beans.builder.ConnectTabPanelModuleBeanBuilder;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * @exampleJson example: {@see com.atlassian.plugin.connect.plugin.capabilities.beans.ConnectJsonExamples#COMPONENT_TAB_PANEL_EXAMPLE}
 * @since 1.0
 */
public class ConnectTabPanelModuleBean extends BeanWithKeyAndParamsAndConditions
{
    private String url;
    private Integer weight;

    public ConnectTabPanelModuleBean() {
        this("", 100, new TabPanelDescriptorHints());
    }

    public ConnectTabPanelModuleBean(String url, Integer weight, TabPanelDescriptorHints descriptorHints)
    {
        this.url = checkNotNull(url);
        this.weight = checkNotNull(weight);
    }

    public ConnectTabPanelModuleBean(ConnectTabPanelModuleBeanBuilder builder)
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

    public static ConnectTabPanelModuleBeanBuilder newTabPanelBean()
    {
        return new ConnectTabPanelModuleBeanBuilder();
    }

    public static ConnectTabPanelModuleBeanBuilder newTabPanelBean(ConnectTabPanelModuleBean defaultBean)
    {
        return new ConnectTabPanelModuleBeanBuilder(defaultBean);
    }
}
