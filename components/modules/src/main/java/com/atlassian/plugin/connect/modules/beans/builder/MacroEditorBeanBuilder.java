package com.atlassian.plugin.connect.modules.beans.builder;

import com.atlassian.plugin.connect.modules.beans.nested.I18nProperty;
import com.atlassian.plugin.connect.modules.beans.nested.MacroEditorBean;

public class MacroEditorBeanBuilder extends BaseModuleBeanBuilder<MacroEditorBeanBuilder, MacroEditorBean> {
    private String url;
    private I18nProperty editTitle;
    private I18nProperty insertTitle;
    private String width;
    private String height;

    public MacroEditorBeanBuilder() {
    }

    public MacroEditorBeanBuilder(MacroEditorBean defaultBean) {
        this.url = defaultBean.getUrl();
        this.editTitle = defaultBean.getEditTitle();
        this.insertTitle = defaultBean.getInsertTitle();
        this.width = defaultBean.getWidth();
        this.height = defaultBean.getHeight();
    }

    public MacroEditorBeanBuilder withUrl(String url) {
        this.url = url;
        return this;
    }

    public MacroEditorBeanBuilder withEditTitle(I18nProperty editTitle) {
        this.editTitle = editTitle;
        return this;
    }

    public MacroEditorBeanBuilder withInsertTitle(I18nProperty insertTitle) {
        this.insertTitle = insertTitle;
        return this;
    }

    public MacroEditorBeanBuilder withWidth(String width) {
        this.width = width;
        return this;
    }

    public MacroEditorBeanBuilder withHeight(String height) {
        this.height = height;
        return this;
    }

    @Override
    public MacroEditorBean build() {
        return new MacroEditorBean(this);
    }
}
