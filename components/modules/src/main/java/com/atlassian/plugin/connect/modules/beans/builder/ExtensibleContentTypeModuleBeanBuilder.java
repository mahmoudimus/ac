package com.atlassian.plugin.connect.modules.beans.builder;

import com.atlassian.plugin.connect.modules.beans.BlueprintModuleBean;
import com.atlassian.plugin.connect.modules.beans.ExtensibleContentTypeAPISupportModuleBean;
import com.atlassian.plugin.connect.modules.beans.ExtensibleContentTypeModuleBean;
import com.atlassian.plugin.connect.modules.beans.ExtensibleContentTypeUISupportModuleBean;
import com.atlassian.plugin.connect.modules.beans.nested.BlueprintTemplateBean;
import com.atlassian.plugin.connect.modules.beans.nested.CreateResultType;
import com.atlassian.plugin.connect.modules.beans.nested.IconBean;

public class ExtensibleContentTypeModuleBeanBuilder extends RequiredKeyBeanBuilder<ExtensibleContentTypeModuleBeanBuilder, ExtensibleContentTypeModuleBean>
{
    private ExtensibleContentTypeUISupportModuleBean uiSupport;
    private ExtensibleContentTypeAPISupportModuleBean apiSupport;

    public ExtensibleContentTypeModuleBeanBuilder()
    {
    }

    public ExtensibleContentTypeModuleBeanBuilder withUISupport(ExtensibleContentTypeUISupportModuleBean uiSupport)
    {
        this.uiSupport = uiSupport;
        return this;
    }

    public ExtensibleContentTypeModuleBeanBuilder withAPISupport(ExtensibleContentTypeAPISupportModuleBean apiSupport)
    {
        this.apiSupport = apiSupport;
        return this;
    }

    public ExtensibleContentTypeModuleBean build()
    {
        return new ExtensibleContentTypeModuleBean(this);
    }
}
