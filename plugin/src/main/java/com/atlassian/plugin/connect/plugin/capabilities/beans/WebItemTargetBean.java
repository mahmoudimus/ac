package com.atlassian.plugin.connect.plugin.capabilities.beans;

import com.atlassian.plugin.connect.plugin.capabilities.beans.builder.BeanWithParamsBuilder;
import com.atlassian.plugin.connect.plugin.capabilities.beans.builder.WebItemTargetBeanBuilder;

import java.util.Collections;
import java.util.Map;

/**
 * @since 1.0
 */
public class WebItemTargetBean extends BeanWithParams
{
    private WebItemTargetType type;
    private Map<String, Object> options;

    public WebItemTargetBean()
    {
        this.type = WebItemTargetType.page;
        this.options = Collections.emptyMap();
    }

    public WebItemTargetBean(final BeanWithParamsBuilder builder)
    {
        super(builder);
        if (null == type)
        {
            type = WebItemTargetType.page;
        }
        if (null == options)
        {
            options = Collections.emptyMap();
        }
    }

    public boolean isPageTarget()
    {
        return WebItemTargetType.page.equals(getType());
    }

    public boolean isDialogTarget()
    {
        return WebItemTargetType.dialog.equals(getType());
    }

    public boolean isInlineDialogTarget()
    {
        return WebItemTargetType.inlineDialog.equals(getType());
    }

    public WebItemTargetType getType()
    {
        return type;
    }

    public Map<String, Object> getOptions()
    {
        return options;
    }

    public static WebItemTargetBeanBuilder newWebItemTargetBean()
    {
        return new WebItemTargetBeanBuilder();
    }

    public static WebItemTargetBeanBuilder newWebItemTargetBean(WebItemTargetBean bean)
    {
        return new WebItemTargetBeanBuilder(bean);
    }
}
