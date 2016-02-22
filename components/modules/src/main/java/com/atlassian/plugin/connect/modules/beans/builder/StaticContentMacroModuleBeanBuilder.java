package com.atlassian.plugin.connect.modules.beans.builder;

import com.atlassian.plugin.connect.modules.beans.StaticContentMacroModuleBean;

public class StaticContentMacroModuleBeanBuilder extends BaseContentMacroModuleBeanBuilder<StaticContentMacroModuleBeanBuilder, StaticContentMacroModuleBean> {
    public StaticContentMacroModuleBeanBuilder() {
    }

    public StaticContentMacroModuleBeanBuilder(StaticContentMacroModuleBean defaultBean) {
        super(defaultBean);
    }

    @Override
    public StaticContentMacroModuleBean build() {
        return new StaticContentMacroModuleBean(this);
    }
}
