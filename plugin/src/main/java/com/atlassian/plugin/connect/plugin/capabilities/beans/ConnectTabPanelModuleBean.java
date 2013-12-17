package com.atlassian.plugin.connect.plugin.capabilities.beans;

import com.atlassian.plugin.connect.plugin.capabilities.beans.builder.ConnectTabPanelModuleBeanBuilder;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Tab panel modules allow add-ons to insert new tabs into various parts of the host applications user interface.
 *
 * The tab panel module takes care of integrating the add-on content into the application for you. The add-on content
 * automatically gets the tab panel styles and decorators from the host application.
 *
 * Json Example:
 * @exampleJson {@see ConnectJsonExamples#COMPONENT_TAB_PANEL_EXAMPLE}
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
        
        if (null == weight)
        {
            this.weight = 100;
        }
        if (null == url)
        {
            this.url = "";
        }
    }

    /**
     *  Specifies the URL targeted by the tab panel. The URL is relative to the add-on's base URL.
     */
    public String getUrl()
    {
        return url;
    }

    /**
     * Determines the order in which the tab panel's link appears in the menu or list.
     *
     * The "lightest" weight (i.e., lowest number) appears first, rising relative to other items,
     * while the "heaviest" weights sink to the bottom of the menu or list.
     *
     * Built-in web items have weights that are incremented by numbers that leave room for additional
     * items, such as by 10 or 100. Be mindful of the weight you choose for your item, so that it appears
     * in a sensible order given existing items.
     */
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
