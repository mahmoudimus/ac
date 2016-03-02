package com.atlassian.plugin.connect.modules.beans.builder.nested.contenttype;

import com.atlassian.plugin.connect.modules.beans.builder.BaseModuleBeanBuilder;
import com.atlassian.plugin.connect.modules.beans.nested.contenttype.IconsBean;
import com.atlassian.plugin.connect.modules.beans.nested.contenttype.UISupportBean;

public class UISupportBeanBuilder
        extends BaseModuleBeanBuilder<UISupportBeanBuilder, UISupportBean> {
    private String contentViewComponent;

    private String contentEditComponent;

    private String containerViewComponent;

    private String titleDisplay;

    private String titleSortValue;

    private IconsBean icons;

    public UISupportBeanBuilder() {
    }

    public UISupportBeanBuilder withContentViewComponent(String viewComponent) {
        this.contentViewComponent = viewComponent;
        return this;
    }

    public UISupportBeanBuilder withContentEditComponent(String editViewComponent) {
        this.contentEditComponent = editViewComponent;
        return this;
    }

    public UISupportBeanBuilder withContainerViewComponent(String containerViewComponent) {
        this.containerViewComponent = containerViewComponent;
        return this;
    }

    public UISupportBeanBuilder withTitleDisplay(String titleDisplay) {
        this.titleDisplay = titleDisplay;
        return this;
    }

    public UISupportBeanBuilder withTitleSortValue(String titleSortValue) {
        this.titleSortValue = titleSortValue;
        return this;
    }

    public UISupportBeanBuilder withIcons(String item, String container, String create) {
        this.icons = new IconsBean(item, container, create);
        return this;
    }

    public UISupportBean build() {
        return new UISupportBean(this);
    }
}
