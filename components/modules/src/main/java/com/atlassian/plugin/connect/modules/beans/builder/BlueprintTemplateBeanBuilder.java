package com.atlassian.plugin.connect.modules.beans.builder;

import com.atlassian.plugin.connect.modules.beans.nested.BlueprintTemplateBean;

public class BlueprintTemplateBeanBuilder<T extends BlueprintTemplateBeanBuilder, B extends BlueprintTemplateBean> {

    private String url;
    private String contextUrl;

    public BlueprintTemplateBeanBuilder()
    {
    }

    public BlueprintTemplateBeanBuilder withUrl(String url)
    {
        this.url = url;
        return this;
    }

    public BlueprintTemplateBeanBuilder withContextUrl(String url)
    {
        this.contextUrl = url;
        return this;
    }

    public B build()
    {
        //noinspection unchecked
        return (B) new BlueprintTemplateBean(this);
    }
}
