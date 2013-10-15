package com.atlassian.plugin.connect.plugin.capabilities.beans;

import com.atlassian.plugin.connect.plugin.capabilities.annotation.CapabilitySet;
import com.atlassian.plugin.connect.plugin.capabilities.beans.builder.WebPanelCapabilityBeanBuilder;
import com.atlassian.plugin.connect.plugin.capabilities.provider.WebPanelModuleProvider;

/**
 * @since version
 */
@CapabilitySet(key = "webPanels", moduleProvider = WebPanelModuleProvider.class)
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
            this.layout = new WebPanelLayout();
        }

        if (null == url)
        {
            this.url = "";
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

    public WebPanelLayout getLayout()
    {
        return layout;
    }

    public String getUrl()
    {
        return url;
    }

    public int getWeight()
    {
        return weight;
    }
    
    public boolean isAbsolute()
    {
        return (null != getUrl() && getUrl().toLowerCase().startsWith("http"));
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
