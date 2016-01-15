package com.atlassian.plugin.connect.modules.beans.builder;

import com.atlassian.plugin.connect.modules.beans.DialogModuleBean;
import com.atlassian.plugin.connect.modules.beans.nested.dialog.DialogOptions;

public class DialogModuleBeanBuilder extends RequiredKeyBeanBuilder<DialogModuleBeanBuilder, DialogModuleBean> {

    private DialogOptions options;

    public DialogModuleBeanBuilder()
    {
    }

    public DialogModuleBeanBuilder(DialogModuleBean bean)
    {
        this.options = bean.getOptions();
    }

    public DialogModuleBeanBuilder withOptions(DialogOptions options)
    {
        this.options = options;
        return this;
    }

    @Override
    public DialogModuleBean build()
    {
        return new DialogModuleBean(this);
    }
}
