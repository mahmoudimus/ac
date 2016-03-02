package com.atlassian.plugin.connect.modules.beans.builder;

import com.atlassian.plugin.connect.modules.beans.nested.BlueprintTemplateBean;
import com.atlassian.plugin.connect.modules.beans.nested.BlueprintTemplateContextBean;

import static com.atlassian.plugin.connect.modules.beans.nested.BlueprintTemplateContextBean.newBlueprintTemplateContextBeanBuilder;

public class BlueprintTemplateBeanBuilder {

    private String url;
    private BlueprintTemplateContextBean blueprintContext;

    public BlueprintTemplateBeanBuilder() {
    }

    public BlueprintTemplateBeanBuilder withUrl(String url) {
        this.url = url;
        return this;
    }

    public BlueprintTemplateBeanBuilder withBlueprintContextUrl(String contextUrl) {
        blueprintContext = newBlueprintTemplateContextBeanBuilder().withUrl(contextUrl).build();
        return this;
    }

    public BlueprintTemplateBeanBuilder withBlueprintContext(BlueprintTemplateContextBean blueprintContext) {
        this.blueprintContext = blueprintContext;
        return this;
    }

    public BlueprintTemplateBean build() {
        //noinspection unchecked
        return new BlueprintTemplateBean(this);
    }
}
