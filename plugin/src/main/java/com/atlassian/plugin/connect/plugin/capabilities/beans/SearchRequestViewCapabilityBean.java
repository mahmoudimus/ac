package com.atlassian.plugin.connect.plugin.capabilities.beans;

import com.atlassian.plugin.connect.plugin.capabilities.beans.builder.SearchRequestViewCapabilityBeanBuilder;
import com.atlassian.plugin.connect.plugin.capabilities.beans.nested.I18nProperty;

import java.net.URI;
import java.net.URISyntaxException;

import static com.atlassian.plugin.connect.plugin.capabilities.beans.nested.I18nProperty.emptyI18nProperty;

public class SearchRequestViewCapabilityBean extends BeanWithKeyAndParamsAndConditions
{
    private Integer weight;
    private String url;
    private I18nProperty description;

    public SearchRequestViewCapabilityBean()
    {
        this.weight = ConnectAddonBean.DEFAULT_WEIGHT;
        this.url = "";
        this.description = emptyI18nProperty();
    }

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
        if (null == description)
        {
            description = emptyI18nProperty();
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

    public I18nProperty getDescription()
    {
        return description;
    }

    public URI createUri() throws URISyntaxException
    {
        return null == url ? null : new URI(url);
    }
}
