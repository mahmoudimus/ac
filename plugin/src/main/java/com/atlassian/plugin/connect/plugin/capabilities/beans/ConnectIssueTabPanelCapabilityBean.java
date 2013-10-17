package com.atlassian.plugin.connect.plugin.capabilities.beans;

import com.atlassian.plugin.connect.plugin.capabilities.annotation.CapabilitySet;
import com.atlassian.plugin.connect.plugin.capabilities.beans.builder.ConnectIssueTabPanelCapabilityBeanBuilder;
import com.atlassian.plugin.connect.plugin.capabilities.provider.ConnectIssueTabPanelModuleProvider;

/**
 * Capabilities bean for Jira Issue Tab Pages. The capability JSON looks like
 * <p>
 * "issueTabPanels": [{
 * "name": {
 *     "value": "My Issue Tab",
 *     "i18n": "my.tab"
 * },
 * "url": "/my-general-page",
 * "icon": {
 *     "width": 16,
 *     "height": 16,
 *     "url": "/some/icon.png"
 * },
 * "weight": 100
}]
 * </p>
 */
@CapabilitySet(key = "issueTabPanels", moduleProvider = ConnectIssueTabPanelModuleProvider.class)
public class ConnectIssueTabPanelCapabilityBean extends NameToKeyBean
{
    private String url;
    private Integer weight;


    public ConnectIssueTabPanelCapabilityBean()
    {
        this.url = "";
        this.weight = 100;
    }

    public ConnectIssueTabPanelCapabilityBean(ConnectIssueTabPanelCapabilityBeanBuilder builder)
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
        
    }

    public String getUrl()
    {
        return url;
    }

    public int getWeight()
    {
        return weight;
    }

    public static ConnectIssueTabPanelCapabilityBeanBuilder newIssueTabPageBean()
    {
        return new ConnectIssueTabPanelCapabilityBeanBuilder();
    }

    public static ConnectIssueTabPanelCapabilityBeanBuilder newIssueTabPageBean(ConnectIssueTabPanelCapabilityBean defaultBean)
    {
        return new ConnectIssueTabPanelCapabilityBeanBuilder(defaultBean);
    }

}
