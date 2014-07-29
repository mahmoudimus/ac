package com.atlassian.plugin.connect.modules.beans.nested.dialog;

import com.atlassian.json.schema.annotation.SchemaDefinition;
import com.atlassian.plugin.connect.modules.beans.WebItemTargetBean;
import com.atlassian.plugin.connect.modules.beans.builder.nested.dialog.DialogOptionsBuilder;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

/**
 * Options for a normal dialog target
 *
 *#### Example
 *
 * @exampleJson {@see com.atlassian.plugin.connect.modules.beans.ConnectJsonExamples#WEBITEM_TARGET_DIALOG_EXAMPLE}
 * @schemaTitle Dialog Options
 * @since 1.1
 */
@SchemaDefinition("dialogOptions")
public class DialogOptions extends BaseDialogOptions implements WebItemTargetOptions
{
    /**
     * Sets how high the dialog is in pixels
     */
    private String height;

    /**
     * Whether chrome should be included in the dialog
     */
    private Boolean chrome;

    public DialogOptions(String width, String height, Boolean chrome)
    {
        super(width);
        this.height = height;
        this.chrome = chrome;
    }

    public DialogOptions()
    {

    }

    public DialogOptions(DialogOptionsBuilder dialogOptionsBuilder)
    {
        super(dialogOptionsBuilder);
    }

    public String getHeight()
    {
        return height;
    }

    public Boolean getChrome()
    {
        return chrome;
    }

    public static DialogOptionsBuilder newDialogOptions()
    {
        return new DialogOptionsBuilder();
    }

    public static DialogOptionsBuilder newDialogOptions(DialogOptions bean)
    {
        return new DialogOptionsBuilder(bean);
    }

    @Override
    public boolean equals(Object otherObj)
    {
        if (otherObj == this)
        {
            return true;
        }

        if (!(otherObj instanceof DialogOptions))
        {
            return false;
        }

        DialogOptions other = (DialogOptions) otherObj;

        return new EqualsBuilder()
                .appendSuper(super.equals(other))
                .append(height, other.height)
                .append(chrome, other.chrome)
                .isEquals();
    }

    @Override
    public int hashCode()
    {
        return new HashCodeBuilder(123, 99)
                .appendSuper(super.hashCode())
                .append(height)
                .append(chrome)
                .build();
    }
}
