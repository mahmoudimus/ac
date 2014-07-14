package com.atlassian.plugin.connect.modules.beans.nested.dialog;

import com.atlassian.json.schema.annotation.SchemaDefinition;
import com.atlassian.plugin.connect.modules.beans.builder.nested.dialog.DialogOptionsBuilder;
import com.atlassian.plugin.connect.modules.beans.builder.nested.dialog.InlineDialogOptionsBuilder;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

/**
 * Inline Dialog Options
 *
 * @schemaTitle Inline Dialog Options
 * @since 1.0
 */
@SchemaDefinition("inlineDialogOptions")
public class InlineDialogOptions extends BaseDialogOptions implements WebItemTargetOptions
{
    /**
     * Determines whether the inline-Dialog will show on a mouseOver or mouseClick of the trigger
     */
    private Boolean onHover;

    /**
     * Determines how long in milliseconds after a show trigger is fired (such as a trigger click) until the dialog is shown
     */
    private Integer showDelay;

    /**
     * Sets an offset distance of the inline-dialog from the trigger element along the x-axis in pixels
     */
    private String offsetX;

    /**
     * Sets an offset distance of the inline-dialog from the trigger element along the y-axis in pixels
     */
    private String offsetY;

    /**
     * Determines if the dialog should be shown relative to where the mouse is at the time of the event trigger (normally a click) if set to false the dialog will show aligned to the left of the trigger with the arrow showing at the center
     */
    private Boolean isRelativeToMouse;

    /**
     * Determines if all other dialogs on the screen are closed when this one is opened
     */
    private Boolean closeOthers;

    /**
     * Determines if the dialog should be shown above the trigger or not. If this option is true but there is insufficient room above the trigger the inline-dialog will be flipped to display below it
     */
    private Boolean onTop;

    /**
     * This option, ignores the 'closeOthers' option
     */
    private Boolean persistent;

    public InlineDialogOptions(Boolean onHover, Integer showDelay,
                               String width, String offsetX, String offsetY,
                               Boolean isRelativeToMouse, Boolean closeOthers,
                               Boolean onTop, Boolean persistent)
    {
        super(width);
        this.onHover = onHover;
        this.showDelay = showDelay;
        this.offsetX = offsetX;
        this.offsetY = offsetY;
        this.isRelativeToMouse = isRelativeToMouse;
        this.closeOthers = closeOthers;
        this.onTop = onTop;
        this.persistent = persistent;
    }

    public InlineDialogOptions()
    {
    }

    public InlineDialogOptions(InlineDialogOptionsBuilder inlineDialogOptionsBuilder)
    {
        super(inlineDialogOptionsBuilder);
    }

    public Boolean getOnHover()
    {
        return onHover;
    }

    public Integer getShowDelay()
    {
        return showDelay;
    }

    public String getOffsetX()
    {
        return offsetX;
    }

    public String getOffsetY()
    {
        return offsetY;
    }

    public Boolean getIsRelativeToMouse()
    {
        return isRelativeToMouse;
    }

    public Boolean getCloseOthers()
    {
        return closeOthers;
    }

    public Boolean getOnTop()
    {
        return onTop;
    }

    public Boolean getPersistent()
    {
        return persistent;
    }

    public static InlineDialogOptionsBuilder newInlineDialogOptions()
    {
        return new InlineDialogOptionsBuilder();
    }

    @Override
    public boolean equals(Object otherObj)
    {
        if (otherObj == this)
        {
            return true;
        }

        if (!(otherObj instanceof InlineDialogOptions /*&& super.equals(otherObj)*/))
        {
            return false;
        }

        InlineDialogOptions other = (InlineDialogOptions) otherObj;

        return new EqualsBuilder()
                .appendSuper(super.equals(other))
                .append(onHover, other.onHover)
                .append(showDelay, other.showDelay)
                .append(offsetX, other.offsetX)
                .append(offsetY, other.offsetY)
                .append(isRelativeToMouse, other.isRelativeToMouse)
                .append(closeOthers, other.closeOthers)
                .append(onTop, other.onTop)
                .append(persistent, other.persistent)
                .isEquals();
    }

    @Override
    public int hashCode()
    {
        return new HashCodeBuilder(123, 99)
                .appendSuper(super.hashCode())
                .append(onHover)
                .append(showDelay)
                .append(offsetX)
                .append(offsetY)
                .append(isRelativeToMouse)
                .append(closeOthers)
                .append(onTop)
                .append(persistent)
                .build();
    }

}
