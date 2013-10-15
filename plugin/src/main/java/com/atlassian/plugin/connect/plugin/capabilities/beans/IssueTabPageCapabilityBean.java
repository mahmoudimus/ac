package com.atlassian.plugin.connect.plugin.capabilities.beans;

import com.atlassian.plugin.connect.plugin.capabilities.annotation.CapabilitySet;
import com.atlassian.plugin.connect.plugin.capabilities.beans.builder.IssueTabPageCapabilityBeanBuilder;
import com.atlassian.plugin.connect.plugin.capabilities.beans.nested.IconBean;
import com.atlassian.plugin.connect.plugin.capabilities.provider.IssueTabPageModuleProvider;

/**
 * @since version
 */
@CapabilitySet(key = "issueTabPanels", moduleProvider = IssueTabPageModuleProvider.class)
public class IssueTabPageCapabilityBean extends NameToKeyBean
{
    /*
    "issueTabPanels": [{
    "name": {
        "value": "My Issue Tab",
        "i18n": "my.tab"
    },
    "url": "/my-general-page",
    "icon": {
        "width": 16,
        "height": 16,
        "url": "/some/icon.png"
    },
    "weight": 100
}]
     */
    private String url;
    private Integer weight;
    private IconBean icon;

    public IssueTabPageCapabilityBean()
    {
        this.url = "";
        this.weight = 100;
        this.icon = IconBean.newIconBean().withWidth(0).withHeight(0).withUrl("").build();
    }

    public IssueTabPageCapabilityBean(IssueTabPageCapabilityBeanBuilder builder)
    {
        super(builder);
        
        if (null == url)
        {
            this.url = "";
        }

        if(null == weight)
        {
            this.weight = 100;
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

    //TODO: Not sure the issueTabPage really has an icon
    public IconBean getIcon()
    {
        return icon;
    }

    public static IssueTabPageCapabilityBeanBuilder newIssueTabPageBean()
    {
        return new IssueTabPageCapabilityBeanBuilder();
    }

    public static IssueTabPageCapabilityBeanBuilder newIssueTabPageBean(IssueTabPageCapabilityBean defaultBean)
    {
        return new IssueTabPageCapabilityBeanBuilder(defaultBean);
    }

}
