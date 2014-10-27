package com.atlassian.plugin.connect.modules.beans;

import com.atlassian.json.schema.annotation.CommonSchemaAttributes;
import com.atlassian.plugin.connect.modules.beans.builder.BaseModuleBeanBuilder;
import com.atlassian.plugin.connect.modules.beans.builder.WebItemTargetBeanBuilder;
import com.atlassian.plugin.connect.modules.beans.nested.dialog.DialogOptions;
import com.atlassian.plugin.connect.modules.beans.nested.dialog.InlineDialogOptions;
import com.atlassian.plugin.connect.modules.beans.nested.dialog.WebItemTargetOptions;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

/**
 * Defines the way a web item link is opened in the browser, such as in a modal or inline dialog.
 *
 *#### Example
 *
 * @exampleJson {@see com.atlassian.plugin.connect.modules.beans.ConnectJsonExamples#WEBITEM_TARGET_INLINE_DIALOG_EXAMPLE}
 * @schemaTitle Web Item Target
 * @since 1.0
 */
public class WebItemTargetBean extends BaseModuleBean
{
    /**
     * Defines how the web-item content should be loaded by the page. By default, the web-item is loaded in the same
     * page. The target can be set to open the web-item url as a modal dialog or an inline dialog.
     */
    @CommonSchemaAttributes (defaultValue = "page")
    private WebItemTargetType type;

    /**
     * An object containing options which vary based on the type of web item target you are implementing. 
     */
    private WebItemTargetOptions options;

    public WebItemTargetBean()
    {
        this.type = WebItemTargetType.page;
    }

    public WebItemTargetBean(final BaseModuleBeanBuilder builder)
    {
        super(builder);
        if (null == type)
        {
            type = WebItemTargetType.page;
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

    public WebItemTargetOptions getOptions()
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

    @Override
    public boolean equals(Object otherObj)
    {
        if (otherObj == this)
        {
            return true;
        }

        if (!(otherObj instanceof WebItemTargetBean))
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
