package com.atlassian.plugin.connect.plugin.capabilities.beans;

import com.atlassian.plugin.connect.plugin.capabilities.beans.builder.ConnectPageCapabilityBeanBuilder;
import com.atlassian.plugin.connect.plugin.capabilities.beans.nested.IconBean;

import static com.atlassian.plugin.connect.plugin.capabilities.beans.WebItemCapabilityBean.newWebItemBean;
import static com.google.common.base.Preconditions.checkNotNull;

public class ConnectPageCapabilityBean extends BeanWithKeyAndParamsAndConditions
{
    private String url;
    private Integer weight;
    private String location;
    private IconBean icon;
//    private String application; TODO: Figure out what to do with application. Only reference to it I see is in DescriptorPermissionsReader. Not sure how that ties in

    public ConnectPageCapabilityBean() {
        this("", 100);
    }

    public ConnectPageCapabilityBean(String url, Integer weight)
    {
        this.url = checkNotNull(url);
        this.weight = checkNotNull(weight);
    }

    public ConnectPageCapabilityBean(ConnectPageCapabilityBeanBuilder builder)
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
        if (null == location)
        {
            this.location = "";
        }
        if (null == icon)
        {
            this.icon = IconBean.newIconBean().withWidth(16).withHeight(16).withUrl("").build();
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

    public IconBean getIcon()
    {
        return icon;
    }

    public String getLocation()
    {
        return location;
    }

    public String getAbsoluteLocation()
    {
        return location; // TODO: is this needed?
    }

    public static ConnectPageCapabilityBeanBuilder newPageBean()
    {
        return new ConnectPageCapabilityBeanBuilder();
    }

    public static ConnectPageCapabilityBeanBuilder newPageBean(ConnectPageCapabilityBean defaultBean)
    {
        return new ConnectPageCapabilityBeanBuilder(defaultBean);
    }
}
