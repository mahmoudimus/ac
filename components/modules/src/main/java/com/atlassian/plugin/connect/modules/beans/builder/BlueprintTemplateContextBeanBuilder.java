package com.atlassian.plugin.connect.modules.beans.builder;

import com.atlassian.plugin.connect.modules.beans.nested.BlueprintTemplateContextBean;

public class BlueprintTemplateContextBeanBuilder<T extends BlueprintTemplateContextBeanBuilder, B extends BlueprintTemplateContextBean> {
    private String url;

    public BlueprintTemplateContextBeanBuilder() {
    }

    public T withUrl(String url) {
        this.url = url;
        return (T) this;
    }

    public B build() {
        return (B) new BlueprintTemplateContextBean(this);
    }
}
