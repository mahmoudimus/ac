package com.atlassian.plugin.connect.plugin.capabilities.beans.builder;

import com.atlassian.plugin.connect.plugin.capabilities.beans.SearchRequestViewCapabilityBean;
import com.atlassian.plugin.connect.plugin.capabilities.beans.nested.I18nProperty;

public class SearchRequestViewCapabilityBeanBuilder extends BeanWithKeyParamsAndConditionsBuilder<SearchRequestViewCapabilityBeanBuilder, SearchRequestViewCapabilityBean>
{
    private Integer weight;
    private String url;
    private I18nProperty description;

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

    public SearchRequestViewCapabilityBeanBuilder withDescription(I18nProperty description)
    {
        this.description = description;
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

    public I18nProperty getDescription()
    {
        return description;
    }

    @Override
    public SearchRequestViewCapabilityBean build()
    {
        return new SearchRequestViewCapabilityBean(this);
    }

}
