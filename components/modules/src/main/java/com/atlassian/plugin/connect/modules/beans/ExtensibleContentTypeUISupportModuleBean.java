package com.atlassian.plugin.connect.modules.beans;

import com.atlassian.plugin.connect.modules.beans.builder.ExtensibleContentTypeUISupportModuleBeanBuilder;

public class ExtensibleContentTypeUISupportModuleBean extends BaseModuleBean
{
    private String viewComponent;

    public ExtensibleContentTypeUISupportModuleBean(ExtensibleContentTypeUISupportModuleBeanBuilder builder)
    {
        super(builder);
    }

    public String getViewComponent()
    {
        return viewComponent;
    }
}
