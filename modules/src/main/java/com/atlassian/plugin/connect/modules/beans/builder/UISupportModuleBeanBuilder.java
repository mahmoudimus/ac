package com.atlassian.plugin.connect.modules.beans.builder;

import com.atlassian.plugin.connect.modules.beans.UISupportModuleBean;
import com.atlassian.plugin.connect.modules.beans.nested.I18nProperty;

public class UISupportModuleBeanBuilder<T extends UISupportModuleBeanBuilder, B extends UISupportModuleBean> extends BaseModuleBeanBuilder<T, B>
{
    private String defaultOperator;
    private I18nProperty name;
    private I18nProperty tooltip;
    private String dataUri;

    public UISupportModuleBeanBuilder()
    {
    }

    public UISupportModuleBeanBuilder(UISupportModuleBean defaultBean)
    {
        this.defaultOperator = defaultBean.getDefaultOperator();
        this.dataUri = defaultBean.getDataUri();
        this.name = defaultBean.getName();
        this.tooltip = defaultBean.getTooltip();
    }

    public T withDefaultOperator(String defaultOperator)
    {
        this.defaultOperator = defaultOperator;
        return (T) this;
    }

    public T withName(I18nProperty name)
    {
        this.name = name;
        return (T) this;
    }

    public T withDataUri(String dataUri)
    {
        this.dataUri = dataUri;
        return (T) this;
    }

    public T withTooltip(I18nProperty tooltip)
    {
        this.tooltip = tooltip;
        return (T) this;
    }

    @Override
    public B build()
    {
        return (B) new UISupportModuleBean(this);
    }
}