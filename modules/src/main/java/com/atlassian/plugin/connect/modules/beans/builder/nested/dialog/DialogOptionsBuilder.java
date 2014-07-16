package com.atlassian.plugin.connect.modules.beans.builder.nested.dialog;

import com.atlassian.plugin.connect.modules.beans.builder.BaseModuleBeanBuilder;
import com.atlassian.plugin.connect.modules.beans.nested.dialog.DialogOptions;

public class DialogOptionsBuilder extends BaseDialogOptionsBuilder<DialogOptionsBuilder, DialogOptions>
{
    private String height;

    public DialogOptionsBuilder()
    {
    }

    public DialogOptionsBuilder(final DialogOptions defaultBean)
    {
        super(defaultBean);
        this.height = defaultBean.getHeight();
    }

    public DialogOptionsBuilder withHeight(String height)
    {
        this.height = height;
        return this;
    }

    @Override
    public DialogOptionsBuilder withWidth(String width)
    {
        return (DialogOptionsBuilder) super.withWidth(width);
    }

    @Override
    public DialogOptions build()
    {
        return new DialogOptions(this);
    }


}
