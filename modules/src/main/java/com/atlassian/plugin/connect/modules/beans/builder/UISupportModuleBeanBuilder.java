package com.atlassian.plugin.connect.modules.beans.builder;

import com.atlassian.plugin.connect.modules.beans.UISupportModuleBean;

public class UISupportModuleBeanBuilder<T extends UISupportModuleBeanBuilder, B extends UISupportModuleBean> extends BaseModuleBeanBuilder<T, B>
{
    // private ValueType valueType; // TODO import this from com.atlassian.querylang.fields
    private String defaultOperator;
    private String i18nKey;
    private String dataUri;
    private String tooltipI18nKey;

    public UISupportModuleBeanBuilder()
    {
    }

    public UISupportModuleBeanBuilder(UISupportModuleBean defaultBean)
    {
        this.defaultOperator = defaultBean.getDefaultOperator();
        this.i18nKey = defaultBean.getI18nKey();
        this.dataUri = defaultBean.getDataUri();
        this.tooltipI18nKey = defaultBean.getTooltipI18nKey();
    }

    public T withDefaultOperator(String defaultOperator)
    {
        this.defaultOperator = defaultOperator;
        return (T) this;
    }

    public T withi18nKey(String i18nKey)
    {
        this.i18nKey = i18nKey;
        return (T) this;
    }

    public T withDataUri(String dataUri)
    {
        this.dataUri = i18nKey;
        return (T) this;
    }

    public T withTooltipI18nKey(String tooltipI18nKey)
    {
        this.tooltipI18nKey = tooltipI18nKey;
        return (T) this;
    }

    @Override
    public B build()
    {
        return (B) new UISupportModuleBean(this);
    }
}