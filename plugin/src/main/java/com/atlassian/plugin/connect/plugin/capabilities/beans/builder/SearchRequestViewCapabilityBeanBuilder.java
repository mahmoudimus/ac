package com.atlassian.plugin.connect.plugin.capabilities.beans.builder;

import com.atlassian.plugin.connect.plugin.capabilities.beans.SearchRequestViewCapabilityBean;

public class SearchRequestViewCapabilityBeanBuilder extends BeanWithKeyParamsAndConditionsBuilder<SearchRequestViewCapabilityBeanBuilder, SearchRequestViewCapabilityBean>
{
    private Integer weight;
    private String url;

    public SearchRequestViewCapabilityBeanBuilder()
    {
    }

    public SearchRequestViewCapabilityBeanBuilder(SearchRequestViewCapabilityBean defaultBean)
    {
        this.weight = defaultBean.getWeight();
        this.url = defaultBean.getUrl();
    }

    public SearchRequestViewCapabilityBeanBuilder withWeight(Integer weight)
    {
        this.weight = weight;
        return this;
    }

    public SearchRequestViewCapabilityBeanBuilder withUrl(String url)
    {
        this.url = url;
        return this;
    }

    public Integer getWeight()
    {
        return weight;
    }

    public String getUrl()
    {
        return url;
    }

    @Override
    public SearchRequestViewCapabilityBean build()
    {
        return new SearchRequestViewCapabilityBean(this);
    }
}
