package com.atlassian.plugin.connect.modules.beans;

import com.atlassian.plugin.connect.modules.beans.builder.UISupportModuleBeanBuilder;
import com.atlassian.plugin.connect.modules.beans.nested.I18nProperty;

public class UISupportModuleBean extends BaseModuleBean
{
    private String defaultOperator;
    private I18nProperty name;
    private I18nProperty tooltip;
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

    public I18nProperty getName()
    {
        return name;
    }

    public String getDataUri()
    {
        return dataUri;
    }

    public I18nProperty getTooltip()
    {
        return tooltip;
    }
}
