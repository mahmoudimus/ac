package com.atlassian.plugin.connect.modules.beans.builder;

import com.atlassian.plugin.connect.modules.beans.nested.BlueprintTemplateBean;

/**
 * Created by mjensen on 7/05/14.
 */
public class BlueprintTemplateBeanBulder<T extends BlueprintTemplateBeanBulder, B extends BlueprintTemplateBean> {

    private String url;
    private String content;

    public BlueprintTemplateBeanBulder()
    {
    }

    public BlueprintTemplateBeanBulder(BlueprintTemplateBean defaultBean)
    {
        this.url = defaultBean.getUrl();
        this.content = defaultBean.getContent();
    }

    public BlueprintTemplateBeanBulder withUrl(String url)
    {
        this.url = url;
        return this;
    }

    public BlueprintTemplateBeanBulder withContent(String content)
    {
        this.content = content;
        return this;
    }

    public B build()
    {
        return (B) new BlueprintTemplateBean(this);
    }

}
