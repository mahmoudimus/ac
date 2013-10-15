package com.atlassian.plugin.connect.plugin.capabilities.beans;

import com.atlassian.plugin.connect.plugin.capabilities.annotation.CapabilitySet;
import com.atlassian.plugin.connect.plugin.capabilities.beans.builder.WebPanelCapabilityBeanBuilder;
import com.atlassian.plugin.connect.plugin.capabilities.beans.builder.WebPanelCapabilityBeanBuilder;
import com.atlassian.plugin.connect.plugin.capabilities.beans.nested.I18nProperty;
import com.atlassian.plugin.connect.plugin.capabilities.beans.nested.IconBean;
import com.atlassian.plugin.connect.plugin.capabilities.provider.WebPanelModuleProvider;

import java.util.ArrayList;
import java.util.List;

/**
 * @since version
 */
@CapabilitySet(key = "web-items", moduleProvider = WebPanelModuleProvider.class)
public class WebPanelCapabilityBean extends NameToKeyBean
{
    private String location;
    private WebPanelLayout layout;
    private String url;
    private Integer weight;

    public WebPanelCapabilityBean()
    {
        this.location = "";
        this.layout = new WebPanelLayout();
        this.url = "";
        this.weight = 100;
    }

    public WebPanelCapabilityBean(WebPanelCapabilityBeanBuilder builder)
    {
        super(builder);

        if (null == location)
        {
            this.location = "";
        }

        if (null == layout)
        {
            this.la
        }

        if(null == weight)
        {
            this.weight = 100;
        }
    }

    public String getLocation()
    {
        return location;
    }

    public int getWeight()
    {
        return weight;
    }
    
    public boolean isAbsolute()
    {
        return (null != getLink() && getLink().startsWith("http"));    
    }
    
    public static WebPanelCapabilityBeanBuilder newWebPanelBean()
    {
        return new WebPanelCapabilityBeanBuilder();
    }

    public static WebPanelCapabilityBeanBuilder newWebPanelBean(WebPanelCapabilityBean defaultBean)
    {
        return new WebPanelCapabilityBeanBuilder(defaultBean);
    }

}
