package com.atlassian.plugin.connect.modules.beans.nested;

import com.atlassian.plugin.connect.modules.beans.BaseModuleBean;
import com.atlassian.plugin.connect.modules.beans.builder.ExtensibleContentTypeUISupportModuleBeanBuilder;

public class ExtensibleContentTypeUISupportModuleBean extends BaseModuleBean
{
    private String viewComponent;
    private I18nProperty typeName;

    public ExtensibleContentTypeUISupportModuleBean(ExtensibleContentTypeUISupportModuleBeanBuilder builder)
    {
        super(builder);
    }

    public String getViewComponent()
    {
        return viewComponent;
    }

    public I18nProperty getTypeName()
    {
        return typeName;
    }
}
