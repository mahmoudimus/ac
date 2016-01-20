package com.atlassian.plugin.connect.modules.beans.builder;

import com.atlassian.plugin.connect.modules.beans.nested.ExtensibleContentTypeAPISupportModuleBean;

public class ExtensibleContentTypeAPISupportModuleBeanBuilder
        extends BaseModuleBeanBuilder<ExtensibleContentTypeAPISupportModuleBeanBuilder, ExtensibleContentTypeAPISupportModuleBean>
{
    private String createUrl;

    public ExtensibleContentTypeAPISupportModuleBeanBuilder()
    {
    }

    public ExtensibleContentTypeAPISupportModuleBeanBuilder withCreateURL(String createUrl)
    {
        this.createUrl = createUrl;
        return this;
    }

    public ExtensibleContentTypeAPISupportModuleBean build()
    {
        return new ExtensibleContentTypeAPISupportModuleBean(this);
    }
}
