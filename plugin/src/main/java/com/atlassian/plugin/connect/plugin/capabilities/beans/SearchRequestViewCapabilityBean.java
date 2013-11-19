package com.atlassian.plugin.connect.plugin.capabilities.beans;

import com.atlassian.plugin.connect.plugin.capabilities.beans.builder.SearchRequestViewCapabilityBeanBuilder;

public class SearchRequestViewCapabilityBean extends BeanWithKeyAndParamsAndConditions
{
    private Integer weight;
    private String url;

    public SearchRequestViewCapabilityBean(SearchRequestViewCapabilityBeanBuilder builder)
    {
        super(builder);

        if (null == weight)
        {
            this.weight = ConnectAddonBean.DEFAULT_WEIGHT;
        }

        if (null == url)
        {
            url = "";
        }
    }

    public static SearchRequestViewCapabilityBeanBuilder newSearchRequestViewCapabilityBean()
    {
        return new SearchRequestViewCapabilityBeanBuilder();
    }

    public static SearchRequestViewCapabilityBeanBuilder newSearchRequestViewCapabilityBean(SearchRequestViewCapabilityBean defaultBean)
    {
        return new SearchRequestViewCapabilityBeanBuilder(defaultBean);
    }

    public Integer getWeight()
    {
        return weight;
    }

    public String getUrl()
    {
        return url;
    }
}
