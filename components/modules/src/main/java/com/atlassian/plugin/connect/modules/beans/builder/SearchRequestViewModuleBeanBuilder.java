package com.atlassian.plugin.connect.modules.beans.builder;

import com.atlassian.plugin.connect.modules.beans.SearchRequestViewModuleBean;
import com.atlassian.plugin.connect.modules.beans.nested.I18nProperty;

public class SearchRequestViewModuleBeanBuilder
        extends BeanWithKeyParamsAndConditionsBuilder<SearchRequestViewModuleBeanBuilder, SearchRequestViewModuleBean>
{
    private Integer weight;
    private String url;
    private I18nProperty description;

    public SearchRequestViewModuleBeanBuilder()
    {
    }

    public SearchRequestViewModuleBeanBuilder(SearchRequestViewModuleBean defaultBean)
    {
        this.weight = defaultBean.getWeight();
        this.url = defaultBean.getUrl();
    }

    public SearchRequestViewModuleBeanBuilder withWeight(Integer weight)
    {
        this.weight = weight;
        return this;
    }

    public SearchRequestViewModuleBeanBuilder withUrl(String url)
    {
        this.url = url;
        return this;
    }

    public SearchRequestViewModuleBeanBuilder withDescription(I18nProperty description)
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
    public SearchRequestViewModuleBean build()
    {
        return new SearchRequestViewModuleBean(this);
    }

}
