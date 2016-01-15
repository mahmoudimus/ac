package com.atlassian.plugin.connect.modules.beans;

import com.atlassian.json.schema.annotation.SchemaDefinition;
import com.atlassian.plugin.connect.modules.beans.builder.DialogModuleBeanBuilder;
import com.atlassian.plugin.connect.modules.beans.builder.RequiredKeyBeanBuilder;
import com.atlassian.plugin.connect.modules.beans.nested.dialog.DialogOptions;
import com.google.common.base.Objects;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

/**
 * <p>Defines a dialog that may be reused by multiple components, e.g. as the target of a web-item.</p>
 *
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
     * TODO - this Javadoc copied from WebItemTargetBean - how to extract? dT
     * <p>An object containing options for this dialog.</p>
     *
     * <div id="modalDialog">
     * <code>Dialog Options</code>
     * <table class="props table table-striped aui">
     *    <thead>
     *        <tr><th>Name</th>
     *        <th>Type</th>
     *        <th>Description</th>
     *    </tr></thead>
     *    <tbody>
     *        <tr>
     *            <td><code>width</code></td>
     *            <td>Number | String</td>
     *            <td>sets how wide the inline-dialog is in pixels</td>
     *        </tr>
     *        <tr>
     *            <td><code>height</code></td>
     *            <td>Number | String</td>
     *            <td>sets how high the inline-dialog is in pixels</td>
     *        </tr>
     *        <tr>
     *            <td><code>chrome</code></td>
     *            <td>Boolean</td>
     *            <td>Whether the dialog should contain the AUI header and buttons. Default is true</td>
     *        </tr>
     *    </tbody>
     * </table>
     * </div>
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
                .append(options, other.options)
                .isEquals();
    }

    @Override
    public int hashCode()
    {
        return new HashCodeBuilder(13, 61)
                .appendSuper(super.hashCode())
                .append(options)
                .build();
    }

}
