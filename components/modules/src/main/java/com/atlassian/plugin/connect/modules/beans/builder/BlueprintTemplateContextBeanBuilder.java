package com.atlassian.plugin.connect.modules.beans.builder;

import com.atlassian.plugin.connect.modules.beans.nested.BlueprintTemplateContextBean;

public class BlueprintTemplateContextBeanBuilder {

    private String url;

    public BlueprintTemplateContextBeanBuilder() {
    }

    public BlueprintTemplateContextBeanBuilder withUrl(String url) {
        this.url = url;
        return this;
    }

    public BlueprintTemplateContextBean build() {
        return new BlueprintTemplateContextBean(this);
    }
}
