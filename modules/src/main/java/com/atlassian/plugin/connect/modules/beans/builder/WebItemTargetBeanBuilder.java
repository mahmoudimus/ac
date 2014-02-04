package com.atlassian.plugin.connect.modules.beans.builder;

import java.util.Map;

import com.atlassian.plugin.connect.modules.beans.WebItemTargetBean;
import com.atlassian.plugin.connect.modules.beans.WebItemTargetType;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.collect.Maps.newHashMap;

public class WebItemTargetBeanBuilder extends BeanWithParamsBuilder<WebItemTargetBeanBuilder, WebItemTargetBean>
{
    private WebItemTargetType type;
    private Map<String, String> options;

    public WebItemTargetBeanBuilder()
    {
        this.type = WebItemTargetType.page;
        this.options = newHashMap();
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

    public WebItemTargetBeanBuilder withOption(String name, String value)
    {
        checkNotNull(options);
        
        options.put(name,value);
        return this;
    }

    public WebItemTargetBeanBuilder withOption(Map<String,String> newOptions)
    {
        checkNotNull(options);

        options.putAll(newOptions);
        return this;
    }

    @Override
    public WebItemTargetBean build()
    {
        return new WebItemTargetBean(this);
    }
}
