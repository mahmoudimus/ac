package com.atlassian.plugin.connect.modules.beans;

import java.util.Map;

import com.atlassian.plugin.connect.modules.beans.builder.BaseModuleBeanBuilder;
import com.atlassian.plugin.connect.modules.beans.builder.BeanWithParamsBuilder;
import com.atlassian.plugin.connect.modules.beans.builder.WebItemTargetBeanBuilder;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import static com.google.common.collect.Maps.newHashMap;

/**
 * Defines the way a web item link is opened in the browser, such as in a modal or inline dialog.
 *
 * #### Example
 *
 * @exampleJson {@see com.atlassian.plugin.connect.modules.beans.ConnectJsonExamples#WEBITEM_TARGET_EXAMPLE}
 * @schemaTitle Web Item Target
 * @since 1.0
 */
public class WebItemTargetBean extends BaseModuleBean
{
    /**
     * Todo
     */
    private WebItemTargetType type;

    /**
     * Todo
     */
    private Map<String, Object> options;

    public WebItemTargetBean()
    {
        this.type = WebItemTargetType.page;
        this.options = newHashMap();
    }

    public WebItemTargetBean(final BaseModuleBeanBuilder builder)
    {
        super(builder);
        if (null == type)
        {
            type = WebItemTargetType.page;
        }
        if(null == options)
        {
            this.options = newHashMap();
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
        if (null == options)
        {
            this.options = newHashMap();
        }
        
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

    @Override
    public boolean equals(Object otherObj)
    {
        if (otherObj == this)
        {
            return true;
        }

        if (!(otherObj instanceof WebItemTargetBean && super.equals(otherObj)))
        {
            return false;
        }

        WebItemTargetBean other = (WebItemTargetBean) otherObj;

        return new EqualsBuilder()
                .append(type, other.type)
                .append(options, other.options)
                .isEquals();
    }

    @Override
    public int hashCode()
    {
        return new HashCodeBuilder(19, 23)
                .appendSuper(super.hashCode())
                .append(type)
                .append(options)
                .build();
    }
}
