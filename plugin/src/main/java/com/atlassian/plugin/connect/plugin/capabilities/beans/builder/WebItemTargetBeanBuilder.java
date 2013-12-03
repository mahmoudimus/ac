package com.atlassian.plugin.connect.plugin.capabilities.beans.builder;

import com.atlassian.plugin.connect.plugin.capabilities.beans.WebItemTargetBean;
import com.atlassian.plugin.connect.plugin.capabilities.beans.WebItemTargetType;

import java.util.Collections;
import java.util.Map;

public class WebItemTargetBeanBuilder extends BaseCapabilityBeanBuilder<WebItemTargetBeanBuilder, WebItemTargetBean>
{
    private WebItemTargetType type;
    private Map<String, Object> options;

    public WebItemTargetBeanBuilder()
    {
        this.type = WebItemTargetType.page;
        this.options = Collections.emptyMap();
    }

    public WebItemTargetBeanBuilder(final WebItemTargetBean defaultBean)
    {
        this.type = defaultBean.getType();
        this.options = defaultBean.getOptions();
    }

    public WebItemTargetBeanBuilder withType(WebItemTargetType type)
    {
        this.type = type;
        return this;
    }

    public WebItemTargetBeanBuilder withOptions(Map<String, Object> options)
    {
        this.options = options;
        return this;
    }

    @Override
    public WebItemTargetBean build()
    {
        return new WebItemTargetBean(this);
    }
}
