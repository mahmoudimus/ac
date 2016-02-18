package com.atlassian.plugin.connect.modules.beans.builder;

import com.atlassian.plugin.connect.modules.beans.nested.contenttype.APISupportBean;
import com.atlassian.plugin.connect.modules.beans.ExtensibleContentTypeModuleBean;
import com.atlassian.plugin.connect.modules.beans.nested.contenttype.OperationSupportBean;
import com.atlassian.plugin.connect.modules.beans.nested.contenttype.UISupportBean;

public class ExtensibleContentTypeModuleBeanBuilder extends RequiredKeyBeanBuilder<ExtensibleContentTypeModuleBeanBuilder, ExtensibleContentTypeModuleBean>
{
    private UISupportBean uiSupport;
    private OperationSupportBean operationSupport;
    private APISupportBean apiSupport;

    public ExtensibleContentTypeModuleBeanBuilder()
    {
    }

    public ExtensibleContentTypeModuleBeanBuilder withUISupport(UISupportBean uiSupport)
    {
        this.uiSupport = uiSupport;
        return this;
    }

    public ExtensibleContentTypeModuleBeanBuilder withOperationSupport(OperationSupportBean operationSupport)
    {
        this.operationSupport = operationSupport;
        return this;
    }

    public ExtensibleContentTypeModuleBeanBuilder withAPISupport(APISupportBean apiSupport)
    {
        this.apiSupport = apiSupport;
        return this;
    }

    public ExtensibleContentTypeModuleBean build()
    {
        return new ExtensibleContentTypeModuleBean(this);
    }
}
