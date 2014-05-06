package com.atlassian.plugin.connect.modules.beans.builder;

import com.atlassian.plugin.connect.modules.beans.BlueprintModuleBean;

public class BlueprintModuleBeanBuilder extends RequiredKeyBeanBuilder<BlueprintModuleBeanBuilder,BlueprintModuleBean> {
    public BlueprintModuleBeanBuilder() {
    }

    public BlueprintModuleBeanBuilder(BlueprintModuleBean bean) {
        super(bean);
    }

    public BlueprintModuleBean build() {
        return new BlueprintModuleBean(this);
    }
}
