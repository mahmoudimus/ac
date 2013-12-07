package com.atlassian.plugin.connect.plugin.capabilities.beans.builder;

import com.atlassian.plugin.connect.plugin.capabilities.beans.WebItemTargetBean;
import com.atlassian.plugin.connect.plugin.capabilities.beans.WebItemTargetType;

import java.util.Collections;
import java.util.Map;

public class WebItemTargetBeanBuilder extends BeanWithParamsBuilder<WebItemTargetBeanBuilder, WebItemTargetBean>
{
    private WebItemTargetType type;

    public WebItemTargetBeanBuilder()
    {
        this.type = WebItemTargetType.page;
    }

    public WebItemTargetBeanBuilder(final WebItemTargetBean defaultBean)
    {
        this.type = defaultBean.getType();
    }

    public WebItemTargetBeanBuilder withType(WebItemTargetType type)
    {
        this.type = type;
        return this;
    }

    @Override
    public WebItemTargetBean build()
    {
        return new WebItemTargetBean(this);
    }
}
