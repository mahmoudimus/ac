package com.atlassian.plugin.connect.modules.beans.builder;

import com.atlassian.plugin.connect.modules.beans.IssueFieldType;
import com.atlassian.plugin.connect.modules.beans.ConnectFieldModuleBean;
import com.atlassian.plugin.connect.modules.beans.nested.I18nProperty;

public class IssueFieldModuleBeanBuilder
        extends RequiredKeyBeanBuilder<IssueFieldModuleBeanBuilder, ConnectFieldModuleBean>
{
    private IssueFieldType type;
    private I18nProperty description;

    public IssueFieldModuleBeanBuilder withBaseType(IssueFieldType type)
    {
        this.type = type;
        return this;
    }

    public IssueFieldModuleBeanBuilder withDescription(I18nProperty description)
    {
        this.description = description;
        return this;
    }

    @Override
    public ConnectFieldModuleBean build()
    {
        return new ConnectFieldModuleBean(this);
    }

}
