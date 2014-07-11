package com.atlassian.plugin.connect.modules.beans.nested.dialog;

import com.atlassian.plugin.connect.modules.beans.builder.nested.dialog.DialogOptionsBuilder;
import com.atlassian.plugin.connect.modules.beans.builder.nested.dialog.InlineDialogOptionsBuilder;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

public class InlineDialogOptions extends BaseDialogOptions
{
    private Boolean onHover;
    private Integer showDelay;
    private String offsetX;
    private String offsetY;
    private Boolean isRelativeToMouse;
    private Boolean closeOthers;
    private Boolean onTop;
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
