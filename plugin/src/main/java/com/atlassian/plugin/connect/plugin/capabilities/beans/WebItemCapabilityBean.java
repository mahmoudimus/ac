package com.atlassian.plugin.connect.plugin.capabilities.beans;

import com.atlassian.plugin.connect.plugin.capabilities.annotation.CapabilitySet;
import com.atlassian.plugin.connect.plugin.capabilities.beans.builder.WebItemCapabilityBeanBuilder;
import com.atlassian.plugin.connect.plugin.capabilities.beans.nested.I18nProperty;
import com.atlassian.plugin.connect.plugin.capabilities.beans.nested.IconBean;
import com.atlassian.plugin.connect.plugin.capabilities.provider.WebItemModuleProvider;
import com.google.common.base.Objects;

import java.util.ArrayList;
import java.util.List;

/**
 * @since version
 */
@CapabilitySet(key = "web-items", moduleProvider = WebItemModuleProvider.class)
public class WebItemCapabilityBean extends NameToKeyBean
{
    private String link;
    private String location;
    private AddOnUrlContext context;
    private Integer weight;
    private Boolean dialog;
    private List<String> styleClasses;
    private I18nProperty tooltip;
    private IconBean icon;

    public WebItemCapabilityBean()
    {
        this.link = "";
        this.location = "";
        this.context = AddOnUrlContext.addon;
        this.weight = 100;
        this.dialog = false;
        this.styleClasses = new ArrayList<String>();
        this.tooltip = new I18nProperty("", "");
        this.icon = IconBean.newIconBean().withWidth(0).withHeight(0).withUrl("").build();
    }

    public WebItemCapabilityBean(WebItemCapabilityBeanBuilder builder)
    {
        super(builder);
        
        if (null == link)
        {
            this.link = "";
        }
        
        if(null == context)
        {
            this.context = AddOnUrlContext.addon;
        }
        
        if(null == weight)
        {
            this.weight = 100;
        }
        
        if(null == dialog)
        {
            this.dialog = false;
        }
        
        if (null == location)
        {
            this.location = "";
        }
        if (null == styleClasses)
        {
            this.styleClasses = new ArrayList<String>();
        }
        if (null == tooltip)
        {
            this.tooltip = new I18nProperty("", "");
        }
        if (null == icon)
        {
            this.icon = IconBean.newIconBean().withWidth(16).withHeight(16).withUrl("").build();
        }
    }

    public String getLink()
    {
        return link;
    }

    public String getLocation()
    {
        return location;
    }

    public AddOnUrlContext getContext()
    {
        return context;
    }

    public int getWeight()
    {
        return weight;
    }
    
    public boolean isDialog()
    {
        return dialog;
    }

    public List<String> getStyleClasses()
    {
        return styleClasses;
    }

    public I18nProperty getTooltip()
    {
        return tooltip;
    }

    public IconBean getIcon()
    {
        return icon;
    }

    public boolean isAbsolute()
    {
        return (null != getLink() && getLink().toLowerCase().startsWith("http"));
    }
    
    public static WebItemCapabilityBeanBuilder newWebItemBean()
    {
        return new WebItemCapabilityBeanBuilder();
    }

    public static WebItemCapabilityBeanBuilder newWebItemBean(WebItemCapabilityBean defaultBean)
    {
        return new WebItemCapabilityBeanBuilder(defaultBean);
    }

    @Override
    public String toString()
    {
        return Objects.toStringHelper(this)
                .add("key", getKey())
                .add("name", getName())
                .add("link", getLink())
                .add("location", getLocation())
                .toString();
    }
}
