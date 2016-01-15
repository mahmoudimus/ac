package com.atlassian.plugin.connect.modules.beans.builder;

import com.atlassian.plugin.connect.modules.beans.WebItemTargetBean;
import com.atlassian.plugin.connect.modules.beans.WebItemTargetType;
import com.atlassian.plugin.connect.modules.beans.nested.dialog.WebItemTargetOptions;

public class WebItemTargetBeanBuilder extends BaseModuleBeanBuilder<WebItemTargetBeanBuilder, WebItemTargetBean>
{
    private WebItemTargetType type;
    private String key;
    private WebItemTargetOptions options;

    public WebItemTargetBeanBuilder()
    {
        this.type = WebItemTargetType.page;
    }

    public WebItemTargetBeanBuilder(final WebItemTargetBean defaultBean)
    {
        this.type = defaultBean.getType();
        this.key = defaultBean.getKey();
        this.options = defaultBean.getOptions();
    }

    public WebItemTargetBeanBuilder withType(WebItemTargetType type)
    {
        this.type = type;
        return this;
    }

    public WebItemTargetBeanBuilder withKey(String key)
    {
        this.key = key;
        return this;
    }

    public WebItemTargetBeanBuilder withOptions(WebItemTargetOptions newOptions)
    {
        this.options = newOptions;
        return this;
    }

    @Override
    public WebItemTargetBean build()
    {
        return new WebItemTargetBean(this);
    }
}
