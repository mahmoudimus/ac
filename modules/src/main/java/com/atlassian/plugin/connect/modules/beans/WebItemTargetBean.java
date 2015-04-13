package com.atlassian.plugin.connect.modules.beans;

import com.atlassian.json.schema.annotation.CommonSchemaAttributes;
import com.atlassian.plugin.connect.modules.beans.builder.BaseModuleBeanBuilder;
import com.atlassian.plugin.connect.modules.beans.builder.WebItemTargetBeanBuilder;
import com.atlassian.plugin.connect.modules.beans.nested.dialog.WebItemTargetOptions;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

/**
 * Defines the way a web item link is opened in the browser, such as in a modal or inline dialog.
 *
 *#### Example
 *
 * @exampleJson {@link com.atlassian.plugin.connect.modules.beans.ConnectJsonExamples#WEBITEM_TARGET_INLINE_DIALOG_EXAMPLE}
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

    // TODO: The table with descriptions is needed until we fix ACDEV-1381
    /**
     * An object containing options which vary based on the type of web item target you are implementing. 
     * <br><br>
     * <div id="inlineDialog" class="aui-expander-content reveal-collapsible">
     * <code>Inline Dialog Options</code>
     * <table class="props table table-striped aui">
     *    <thead>
     *        <th>Name</th>
     *        <th>Type</th>
     *        <th>Description</th>
     *    </thead>
     *    <tbody>
     *        <tr>
     *            <td><code>onHover</code></td>
     *            <td>Boolean</td>
     *            <td>determines whether the inline-Dialog will show on a mouseOver or mouseClick of the trigger</td>
     *        </tr>
     *        <tr>
     *            <td><code>showDelay</code></td>
     *            <td>Number | String</td>
     *            <td>determines how long in milliseconds after a show trigger is fired (such as a trigger click) until the dialog is shown</td>
     *        </tr>
     *        <tr>
     *            <td><code>width</code></td>
     *            <td>Number | String</td>
     *            <td>sets how wide the inline-dialog is in pixels</td>
     *        </tr>
     *        <tr>
     *            <td><code>offsetX</code></td>
     *            <td>Number | String</td>
     *            <td>sets an offset distance of the inline-dialog from the trigger element along the x-axis in pixels</td>
     *        </tr>
     *        <tr>
     *            <td><code>offsetY</code></td>
     *            <td>Number | String</td>
     *            <td>sets an offset distance of the inline-dialog from the trigger element along the y-axis in pixels</td>
     *        </tr>
     *        <tr>
     *            <td><code>isRelativeToMouse</code></td>
     *            <td>Boolean</td>
     *            <td>determines if the dialog should be shown relative to where the mouse is at the time of the event trigger (normally a click) if set to false the dialog will show aligned to the left of the trigger with the arrow showing at the center</td>
     *        </tr>
     *        <tr>
     *            <td><code>closeOthers</code></td>
     *            <td>Boolean</td>
     *            <td>determines if all other dialogs on the screen are closed when this one is opened</td>
     *        </tr>
     *        <tr>
     *            <td><code>onTop</code></td>
     *            <td>Boolean</td>
     *            <td>determines if the dialog should be shown above the trigger or not. If this option is true but there is insufficient room above the trigger the inline-dialog will be flipped to display below it</td>
     *        </tr>
     *        <tr>
     *            <td><code>persistent</code></td>
     *            <td>Boolean</td>
     *            <td>this option, ignores the 'closeOthers' option</td>
     *        </tr>
     *    </tbody>
     * </table>
     * <a id="reveal-text-trigger1" data-replace-text="Collapse" data-replace-selector=".reveal-text-trigger-text" class="aui-expander-trigger aui-expander-reveal-text" aria-controls="inlineDialog">
     *     <p class="reveal-text-trigger-text">Expand</p>
     * </a>
     * </div>
     *
     * <div id="modalDialog" class="aui-expander-content reveal-collapsible">
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
     * <a id="reveal-text-trigger2" data-replace-text="Collapse" data-replace-selector=".reveal-text-trigger-text" class="aui-expander-trigger aui-expander-reveal-text" aria-controls="modalDialog">
     *     <p class="reveal-text-trigger-text">Expand</p>
     * </a>
     * </div>
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
