package com.atlassian.plugin.connect.plugin.capabilities.beans.builder;

import com.atlassian.plugin.connect.plugin.capabilities.beans.StaticContentMacroModuleBean;
import com.atlassian.plugin.connect.plugin.capabilities.beans.nested.MacroHttpMethod;

public class StaticContentMacroModuleBeanBuilder extends BaseContentMacroModuleBeanBuilder<StaticContentMacroModuleBeanBuilder, StaticContentMacroModuleBean>
{
    private MacroHttpMethod method;

    public StaticContentMacroModuleBeanBuilder()
    {
    }

    public StaticContentMacroModuleBeanBuilder(StaticContentMacroModuleBean defaultBean)
    {
        super(defaultBean);
        this.method = defaultBean.getMethod();
    }

    public StaticContentMacroModuleBeanBuilder withMethod(MacroHttpMethod method)
    {
        this.method = method;
        return this;
    }

    @Override
    public StaticContentMacroModuleBean build()
    {
        return new StaticContentMacroModuleBean(this);
    }
}
