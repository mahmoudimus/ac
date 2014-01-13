package com.atlassian.plugin.connect.modules.beans.builder;

import com.atlassian.plugin.connect.modules.beans.StaticContentMacroModuleBean;
import com.atlassian.plugin.connect.modules.beans.nested.MacroHttpMethod;

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
