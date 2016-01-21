package com.atlassian.plugin.connect.modules.beans.builder;

import com.atlassian.plugin.connect.modules.beans.nested.ExtensibleContentTypeUISupportModuleBean;
import com.atlassian.plugin.connect.modules.beans.nested.I18nProperty;

public class ExtensibleContentTypeUISupportModuleBeanBuilder
        extends BaseModuleBeanBuilder<ExtensibleContentTypeUISupportModuleBeanBuilder, ExtensibleContentTypeUISupportModuleBean>
{
    private String viewComponent;
    private I18nProperty typeName;

    public ExtensibleContentTypeUISupportModuleBeanBuilder()
    {
    }

    public ExtensibleContentTypeUISupportModuleBeanBuilder withViewComponent(String viewComponent)
    {
        this.viewComponent = viewComponent;
        return this;
    }

    public ExtensibleContentTypeUISupportModuleBeanBuilder withTypeName(I18nProperty typeName)
    {
        this.typeName = typeName;
        return this;
    }

    public ExtensibleContentTypeUISupportModuleBean build()
    {
        return new ExtensibleContentTypeUISupportModuleBean(this);
    }
}
