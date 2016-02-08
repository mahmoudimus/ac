package com.atlassian.plugin.connect.modules.beans;

import com.atlassian.json.schema.annotation.Required;
import com.atlassian.json.schema.annotation.SchemaDefinition;
import com.atlassian.json.schema.annotation.StringSchemaAttributes;
import com.atlassian.plugin.connect.modules.beans.builder.DialogModuleBeanBuilder;
import com.atlassian.plugin.connect.modules.beans.builder.RequiredKeyBeanBuilder;
import com.atlassian.plugin.connect.modules.beans.nested.dialog.DialogOptions;
import com.google.common.base.Objects;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

/**
 * <p>A common dialog definition that may be referenced by other Connect modules and from JavaScript API calls.</p>
 *
 * <p>
 *     For example, a dialog defined in this module might be referenced from a web item target, so that
 *     triggering the web item launches the dialog.
 * </p>
 *#### Example
 *
 * @exampleJson {@link com.atlassian.plugin.connect.modules.beans.ConnectJsonExamples#DIALOG_EXAMPLE}
 * @schemaTitle Dialog
 * @since 1.0
 */
@SchemaDefinition("dialog")
public class DialogModuleBean extends RequiredKeyBean
{
    /**
     * Specifies the URL of the content displayed in the dialog. The URL can be absolute or relative to either the
     * product URL or the add-on's base URL, depending on the _context_ attribute.
     *
     * Your add-on can receive <a href="../../concepts/context-parameters.html">additional context</a> from the application by
     * using variable tokens in the URL attribute.
     */
    @Required
    @StringSchemaAttributes(format = "uri-template")
    private String url;

    /**
     * <p>An object containing options for this dialog.</p>
     */
    private DialogOptions options;

    public DialogModuleBean(final RequiredKeyBeanBuilder builder)
    {
        super(builder);
        if (null == options)
        {
            options = DialogOptions.newDialogOptions().build();
        }
    }

    public String getUrl()
    {
        return url;
    }

    public DialogOptions getOptions()
    {
        return options;
    }

    public static DialogModuleBeanBuilder newDialogBean()
    {
        return new DialogModuleBeanBuilder();
    }

    public static DialogModuleBeanBuilder newDialogBean(DialogModuleBean defaultBean)
    {
        return new DialogModuleBeanBuilder(defaultBean);
    }

    @Override
    public String toString()
    {
        return Objects.toStringHelper(this)
                .add("key", getRawKey())
                .add("url", getUrl())
                .add("name", getName())
                .add("options", getOptions())
                .toString();
    }

    @Override
    public boolean equals(Object otherObj)
    {
        if (otherObj == this)
        {
            return true;
        }

        if (!(otherObj instanceof DialogModuleBean && super.equals(otherObj)))
        {
            return false;
        }

        DialogModuleBean other = (DialogModuleBean) otherObj;

        return new EqualsBuilder()
                .append(getUrl(), other.getUrl())
                .append(options, other.options)
                .isEquals();
    }

    @Override
    public int hashCode()
    {
        return new HashCodeBuilder(13, 61)
                .appendSuper(super.hashCode())
                .append(url)
                .append(options)
                .build();
    }

}
