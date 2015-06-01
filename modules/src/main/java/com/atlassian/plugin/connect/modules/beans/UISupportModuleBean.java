package com.atlassian.plugin.connect.modules.beans;

import com.atlassian.plugin.connect.modules.beans.builder.UISupportModuleBeanBuilder;

public class UISupportModuleBean extends BaseModuleBean
{
    private String defaultOperator;
    private String i18nKey;
    private String tooltipI18nKey;
    private String dataUri;

    public UISupportModuleBean(UISupportModuleBeanBuilder builder) {
        super(builder);
    }

    public static UISupportModuleBeanBuilder newUISupportModuleBean()
    {
        return new UISupportModuleBeanBuilder<>();
    }

    public static UISupportModuleBeanBuilder newUISupportModuleBean(UISupportModuleBean defaultBean)
    {
        return new UISupportModuleBeanBuilder(defaultBean);
    }

    public String getDefaultOperator()
    {
        return defaultOperator;
    }

    public String getI18nKey()
    {
        return i18nKey;
    }

    public String getDataUri()
    {
        return dataUri;
    }

    public String getTooltipI18nKey()
    {
        return tooltipI18nKey;
    }
}
