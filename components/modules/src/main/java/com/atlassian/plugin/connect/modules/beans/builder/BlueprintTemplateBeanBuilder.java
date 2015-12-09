package com.atlassian.plugin.connect.modules.beans.builder;

import com.atlassian.plugin.connect.modules.beans.nested.BlueprintTemplateBean;
import com.atlassian.plugin.connect.modules.beans.nested.BlueprintTemplateContextBean;

import static com.atlassian.plugin.connect.modules.beans.nested.BlueprintTemplateContextBean.newBlueprintTemplateContextBeanBuilder;

public class BlueprintTemplateBeanBuilder<T extends BlueprintTemplateBeanBuilder, B extends BlueprintTemplateBean> {

    private String url;
    private BlueprintTemplateContextBean blueprintContext;

    public BlueprintTemplateBeanBuilder()
    {
    }

    public T withUrl(String url)
    {
        this.url = url;
        return (T) this;
    }

    public T withBlueprintContextUrl(String contextUrl)
    {
        blueprintContext = newBlueprintTemplateContextBeanBuilder().withUrl(contextUrl).build();
        return (T) this;
    }
    public T withBlueprintContext(BlueprintTemplateContextBean blueprintContext)
    {
        this.blueprintContext = blueprintContext;
        return (T) this;
    }

    public B build()
    {
        //noinspection unchecked
        return (B) new BlueprintTemplateBean(this);
    }
}
