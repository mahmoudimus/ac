package com.atlassian.plugin.connect.modules.beans.builder.nested.dialog;

import com.atlassian.plugin.connect.modules.beans.nested.dialog.DialogOptions;
import com.atlassian.plugin.connect.modules.beans.nested.dialog.InlineDialogOptions;

public class InlineDialogOptionsBuilder extends BaseDialogOptionsBuilder<InlineDialogOptionsBuilder, InlineDialogOptions>
{
    private Boolean onHover;
    private Integer showDelay;
    private String offsetX;
    private String offsetY;
    private Boolean isRelativeToMouse;
    private Boolean closeOthers;
    private Boolean onTop;
    private Boolean persistent;

    public InlineDialogOptionsBuilder()
    {
    }

    public InlineDialogOptionsBuilder(final InlineDialogOptions defaultBean)
    {
        super(defaultBean);
        this.onHover = defaultBean.getOnHover();
        this.showDelay = defaultBean.getShowDelay();
        this.offsetX = defaultBean.getOffsetX();
        this.offsetY = defaultBean.getOffsetY();
        this.isRelativeToMouse = defaultBean.getIsRelativeToMouse();
        this.closeOthers = defaultBean.getCloseOthers();
        this.onTop = defaultBean.getOnTop();
        this.persistent = defaultBean.getPersistent();
    }

    public InlineDialogOptionsBuilder withOnHover(Boolean onHover)
    {
        this.onHover = onHover;
        return this;
    }

    @Override
    public InlineDialogOptionsBuilder withWidth(String width)
    {
        return (InlineDialogOptionsBuilder) super.withWidth(width);
    }

    public InlineDialogOptionsBuilder withShowDelay(Integer showDelay)
    {
        this.showDelay = showDelay;
        return this;
    }

    public InlineDialogOptionsBuilder withOffsetX(String offsetX)
    {
        this.offsetX = offsetX;
        return this;
    }

    public InlineDialogOptionsBuilder withOffsetY(String offsetY)
    {
        this.offsetY = offsetY;
        return this;
    }

    public InlineDialogOptionsBuilder withIsRelativeToMouse(Boolean isRelativeToMouse)
    {
        this.isRelativeToMouse = isRelativeToMouse;
        return this;
    }

    public InlineDialogOptionsBuilder withCloseOthers(Boolean closeOthers)
    {
        this.closeOthers = closeOthers;
        return this;
    }

    public InlineDialogOptionsBuilder withOnTop(Boolean onTop)
    {
        this.onTop = onTop;
        return this;
    }

    public InlineDialogOptionsBuilder withPersistent(Boolean persistent)
    {
        this.persistent = persistent;
        return this;
    }

    @Override
    public InlineDialogOptions build()
    {
        return new InlineDialogOptions(this);
    }


}
