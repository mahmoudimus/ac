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
 * <p>Defines a dialog that may be reused by multiple components, e.g. as the target of a web item.</p>
 *
 * <p>
 *     The specified dialog uses the same options allowed by the Connect Dialog [JavaScript API](https://developer.atlassian.com/static/connect/docs/latest/javascript/module-Dialog.html#.create),
 *     which in turn uses options provided by the Atlassian User Interface [dialog component](https://docs.atlassian.com/aui/latest/docs/dialog.html)
 * </p>
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
     * Specifies the URL of the content displayed in the dialog. The URL can be absolute or relative to either the
     * product URL or the add-on's base URL, depending on the _context_ attribute.
     *
     * Your add-on can receive [additional context](../../concepts/context-parameters.html) from the application by
     * using variable tokens in the URL attribute.
     */
    @Required
    @StringSchemaAttributes(format = "uri-template")
    private String url;

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
     *            <td><code>size</code></td>
     *            <td>String</td>
     *            <td>
     *                sets the size of the dialog without needing width and height specified. Options are
     *                small, medium, large, x-large or fullscreen. Fullscreen-size dialogs will always show a header
     *                with buttons.
     *            </td>
     *        </tr>
     *        <tr>
     *            <td><code>width</code></td>
     *            <td>Number | String</td>
     *            <td>sets how wide the dialog is in pixels</td>
     *        </tr>
     *        <tr>
     *            <td><code>height</code></td>
     *            <td>Number | String</td>
     *            <td>sets how high the dialog is in pixels</td>
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
