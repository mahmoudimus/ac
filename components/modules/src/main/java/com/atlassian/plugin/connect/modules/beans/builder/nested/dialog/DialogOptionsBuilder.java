package com.atlassian.plugin.connect.modules.beans.builder.nested.dialog;

import com.atlassian.plugin.connect.modules.beans.nested.dialog.DialogOptions;
import com.atlassian.plugin.connect.modules.beans.nested.dialog.DialogSize;

public class DialogOptionsBuilder extends BaseDialogOptionsBuilder<DialogOptionsBuilder, DialogOptions>
{
    private DialogSize size;
    private String height;
    private Boolean chrome;

    public DialogOptionsBuilder()
    {
    }

    public DialogOptionsBuilder(final DialogOptions defaultBean)
    {
        super(defaultBean);
        this.size = defaultBean.getSize();
        this.height = defaultBean.getHeight();
    }

    public DialogOptionsBuilder withSize(DialogSize size)
    {
        this.size = size;
        return this;
    }

    public DialogOptionsBuilder withHeight(String height)
    {
        this.height = height;
        return this;
    }

    public DialogOptionsBuilder withChrome(boolean chrome)
    {
        this.chrome = chrome;
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
