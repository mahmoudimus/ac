package com.atlassian.plugin.connect.modules.beans.builder;

import com.atlassian.plugin.connect.modules.beans.BlueprintModuleBean;

public class BlueprintModuleBeanBuilder extends RequiredKeyBeanBuilder<BlueprintModuleBeanBuilder,BlueprintModuleBean> {

    private String url;

    public BlueprintModuleBeanBuilder() { }

    public BlueprintModuleBeanBuilder(BlueprintModuleBean bean) {
        super(bean);
    }

    public BlueprintModuleBeanBuilder withUrl(String url) {
        this.url = url;
        return this;
    }

    public BlueprintModuleBean build() {
        return new BlueprintModuleBean(this);
    }
}
