package com.atlassian.plugin.connect.modules.beans.builder;

import com.atlassian.plugin.connect.modules.beans.WebItemTargetBean;
import com.atlassian.plugin.connect.modules.beans.WebItemTargetType;

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
