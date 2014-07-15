package com.atlassian.plugin.connect.modules.beans.builder.nested.dialog;

import com.atlassian.plugin.connect.modules.beans.builder.BaseModuleBeanBuilder;
import com.atlassian.plugin.connect.modules.beans.nested.dialog.BaseDialogOptions;

public abstract class BaseDialogOptionsBuilder<B extends BaseDialogOptionsBuilder,
        T extends BaseDialogOptions> extends BaseModuleBeanBuilder<B, T>
{
    private String width;

    public BaseDialogOptionsBuilder()
    {
    }

    public BaseDialogOptionsBuilder(final BaseDialogOptions defaultBean)
    {
        this.width = defaultBean.getWidth();
    }

    public BaseDialogOptionsBuilder withWidth(String width)
    {
        this.width = width;
        return this;
    }
}
