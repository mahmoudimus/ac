package com.atlassian.plugin.connect.modules.beans.nested.dialog;

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
}
