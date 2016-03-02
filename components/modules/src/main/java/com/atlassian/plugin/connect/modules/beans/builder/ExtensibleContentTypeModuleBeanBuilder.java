package com.atlassian.plugin.connect.modules.beans.builder;

import com.atlassian.plugin.connect.modules.beans.ExtensibleContentTypeModuleBean;
import com.atlassian.plugin.connect.modules.beans.nested.contenttype.APISupportBean;

public class ExtensibleContentTypeModuleBeanBuilder extends RequiredKeyBeanBuilder<ExtensibleContentTypeModuleBeanBuilder, ExtensibleContentTypeModuleBean> {
    private APISupportBean apiSupport;

    public ExtensibleContentTypeModuleBeanBuilder() {
    }

    public ExtensibleContentTypeModuleBeanBuilder withAPISupport(APISupportBean apiSupport) {
        this.apiSupport = apiSupport;
        return this;
    }

    public ExtensibleContentTypeModuleBean build() {
        return new ExtensibleContentTypeModuleBean(this);
    }
}
