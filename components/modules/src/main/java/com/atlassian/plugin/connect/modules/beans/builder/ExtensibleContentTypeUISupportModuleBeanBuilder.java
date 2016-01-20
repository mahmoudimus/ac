package com.atlassian.plugin.connect.modules.beans.builder;

import com.atlassian.plugin.connect.modules.beans.nested.ExtensibleContentTypeUISupportModuleBean;

public class ExtensibleContentTypeUISupportModuleBeanBuilder
        extends BaseModuleBeanBuilder<ExtensibleContentTypeUISupportModuleBeanBuilder, ExtensibleContentTypeUISupportModuleBean>
{
    private String viewComponent;

    public ExtensibleContentTypeUISupportModuleBeanBuilder()
    {
    }

    public ExtensibleContentTypeUISupportModuleBeanBuilder withViewComponent(String viewComponent)
    {
        this.viewComponent = viewComponent;
        return this;
    }

    public ExtensibleContentTypeUISupportModuleBean build()
    {
        return new ExtensibleContentTypeUISupportModuleBean(this);
    }
}
