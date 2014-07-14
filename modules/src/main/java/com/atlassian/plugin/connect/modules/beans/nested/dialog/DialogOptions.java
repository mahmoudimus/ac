package com.atlassian.plugin.connect.modules.beans.nested.dialog;

import com.atlassian.json.schema.annotation.SchemaDefinition;
import com.atlassian.plugin.connect.modules.beans.WebItemTargetBean;
import com.atlassian.plugin.connect.modules.beans.builder.nested.dialog.DialogOptionsBuilder;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

/**
 * Dialog Options
 *
 * @schemaTitle Dialog Options
 * @since 1.0
 */
@SchemaDefinition("dialogOptions")
public class DialogOptions extends BaseDialogOptions implements WebItemTargetOptions
{
    /**
     * Sets how high the dialog is in pixels
     */
    private String height;

    public DialogOptions(String width, String height)
    {
        super(width);
        this.height = height;
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

        if (!(otherObj instanceof DialogOptions /*&& super.equals(otherObj)*/))
        {
            return false;
        }

        DialogOptions other = (DialogOptions) otherObj;

        return new EqualsBuilder()
                .appendSuper(super.equals(other))
                .append(height, other.height)
                .isEquals();
    }

    @Override
    public int hashCode()
    {
        return new HashCodeBuilder(123, 99)
                .appendSuper(super.hashCode())
                .append(height)
                .build();
    }


}
