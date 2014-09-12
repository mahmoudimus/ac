package com.atlassian.plugin.connect.modules.beans.builder;

import com.atlassian.plugin.connect.modules.beans.nested.BlueprintTemplateBean;

/**
 * Created by mjensen on 7/05/14.
 */
public class BlueprintTemplateBeanBuilder<T extends BlueprintTemplateBeanBuilder, B extends BlueprintTemplateBean> {

    private String url;
    private String content;

    public BlueprintTemplateBeanBuilder()
    {
    }

    public BlueprintTemplateBeanBuilder(BlueprintTemplateBean defaultBean)
    {
        this.url = defaultBean.getUrl();
        this.content = defaultBean.getContent();
    }

    public BlueprintTemplateBeanBuilder withUrl(String url)
    {
        this.url = url;
        return this;
    }

    public BlueprintTemplateBeanBuilder withContent(String content)
    {
        this.content = content;
        return this;
    }

    public B build()
    {
        return (B) new BlueprintTemplateBean(this);
    }

}
