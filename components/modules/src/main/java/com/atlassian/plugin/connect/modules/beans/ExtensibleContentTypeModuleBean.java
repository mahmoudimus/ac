package com.atlassian.plugin.connect.modules.beans;

import com.atlassian.json.schema.annotation.Required;
import com.atlassian.plugin.connect.modules.beans.builder.ExtensibleContentTypeModuleBeanBuilder;
import com.atlassian.plugin.connect.modules.beans.nested.contenttype.APISupportBean;
import com.atlassian.plugin.connect.modules.beans.nested.contenttype.OperationSupportBean;
import com.atlassian.plugin.connect.modules.beans.nested.contenttype.UISupportBean;

import org.apache.commons.lang3.ObjectUtils;

public class ExtensibleContentTypeModuleBean extends RequiredKeyBean
{
    @Required
    private UISupportBean uiSupport;

    private OperationSupportBean operationSupport;

    @Required
    private APISupportBean apiSupport;

    public ExtensibleContentTypeModuleBean()
    {
        initialise();
    }

    public ExtensibleContentTypeModuleBean(ExtensibleContentTypeModuleBeanBuilder builder)
    {
        super(builder);
        initialise();
    }

    private void initialise()
    {
        operationSupport = ObjectUtils.defaultIfNull(operationSupport, new OperationSupportBean());
    }

    public UISupportBean getUiSupport()
    {
        return uiSupport;
    }

    public OperationSupportBean getOperationSupport()
    {
        return operationSupport;
    }

    public APISupportBean getApiSupport()
    {
        return apiSupport;
    }

    public String getModuleKey()
    {
        return getRawKey();
    }
}
