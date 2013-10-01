package com.atlassian.plugin.connect.plugin.capabilities.beans;

import java.util.ArrayList;
import java.util.List;

import com.atlassian.plugin.connect.api.capabilities.annotation.CapabilitySet;
import com.atlassian.plugin.connect.plugin.capabilities.beans.builder.WebItemCapabilityBeanBuilder;
import com.atlassian.plugin.connect.plugin.capabilities.provider.WebItemModuleProvider;

import static com.atlassian.plugin.connect.plugin.capabilities.beans.IconCapabilityBean.newIconCapabilityBean;

/**
 * @since version
 */
@CapabilitySet(key = "web-items", moduleProvider = WebItemModuleProvider.class)
public class WebItemCapabilityBean extends BaseCapabilityBean
{
    private String link;
    private String section;
    private int weight;
    private List<String> styleClasses;
    private I18nProperty tooltip;
    private IconCapabilityBean icon;

    public WebItemCapabilityBean()
    {
        this.link = "";
        this.section = "";
        this.weight = 100;
        this.styleClasses = new ArrayList<String>();
        this.tooltip = new I18nProperty("", "");
        this.icon = newIconCapabilityBean().withWidth(16).withHeight(16).withUrl("").build();
    }

    public WebItemCapabilityBean(WebItemCapabilityBeanBuilder builder)
    {
        super(builder);

        if (null == link)
        {
            this.link = "";
        }
        if (null == section)
        {
            this.section = "";
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
            this.icon = newIconCapabilityBean().withWidth(16).withHeight(16).withUrl("").build();
        }
    }

    public String getLink()
    {
        return link;
    }

    public String getSection()
    {
        return section;
    }

    public int getWeight()
    {
        return weight;
    }

    public List<String> getStyleClasses()
    {
        return styleClasses;
    }

    public I18nProperty getTooltip()
    {
        return tooltip;
    }

    public IconCapabilityBean getIcon()
    {
        return icon;
    }

    public boolean isAbsolute()
    {
        return (null != getLink() && getLink().startsWith("http"));    
    }
    
    public static WebItemCapabilityBeanBuilder newWebItemBean()
    {
        return new WebItemCapabilityBeanBuilder();
    }

    public static WebItemCapabilityBeanBuilder newWebItemBean(WebItemCapabilityBean defaultBean)
    {
        return new WebItemCapabilityBeanBuilder(defaultBean);
    }

}
