package com.atlassian.plugin.connect.modules.beans;

import com.atlassian.plugin.connect.modules.beans.builder.ExtensibleContentTypeModuleBeanBuilder;
import com.atlassian.plugin.connect.modules.beans.nested.ExtensibleContentTypeAPISupportModuleBean;
import com.atlassian.plugin.connect.modules.beans.nested.ExtensibleContentTypeUISupportModuleBean;

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
