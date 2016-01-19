package com.atlassian.plugin.connect.modules.beans;

import com.atlassian.plugin.connect.modules.beans.builder.ExtensibleContentTypeModuleBeanBuilder;

public class ExtensibleContentTypeModuleBean extends RequiredKeyBean
{
    private ExtensibleContentTypeUISupportModuleBean uiSupport;
    private ExtensibleContentTypeAPISupportModuleBean apiSupport;

    public ExtensibleContentTypeModuleBean() {
        initialise();
    }

    public ExtensibleContentTypeModuleBean(ExtensibleContentTypeModuleBeanBuilder builder)
    {
        super(builder);
        initialise();
    }

    private void initialise()
    {
    }

    public ExtensibleContentTypeUISupportModuleBean getUiSupport()
    {
        return uiSupport;
    }

    public ExtensibleContentTypeAPISupportModuleBean getApiSupport()
    {
        return apiSupport;
    }
}
