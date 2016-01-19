package com.atlassian.plugin.connect.modules.beans;

import com.atlassian.plugin.connect.modules.beans.builder.ExtensibleContentTypeModuleBeanBuilder;
import com.atlassian.plugin.connect.modules.beans.nested.ExtensibleContentTypeAPISupportModuleBean;
import com.atlassian.plugin.connect.modules.beans.nested.ExtensibleContentTypeUISupportModuleBean;

import org.apache.commons.lang3.ObjectUtils;

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
        uiSupport = ObjectUtils.defaultIfNull(uiSupport, new ExtensibleContentTypeUISupportModuleBean());
        apiSupport = ObjectUtils.defaultIfNull(apiSupport, new ExtensibleContentTypeAPISupportModuleBean());
    }

    public ExtensibleContentTypeUISupportModuleBean getUiSupport()
    {
        return uiSupport;
    }

    public ExtensibleContentTypeAPISupportModuleBean getApiSupport()
    {
        return apiSupport;
    }

    public String getModuleKey()
    {
        return getRawKey();
    }
}
