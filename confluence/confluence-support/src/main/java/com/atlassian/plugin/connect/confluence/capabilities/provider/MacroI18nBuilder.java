package com.atlassian.plugin.connect.confluence.capabilities.provider;

import com.atlassian.plugin.connect.modules.beans.BaseContentMacroModuleBean;
import com.atlassian.plugin.connect.modules.beans.ConnectAddonBean;
import com.atlassian.plugin.connect.modules.beans.nested.I18nProperty;
import com.atlassian.plugin.connect.modules.beans.nested.MacroParameterBean;
import org.apache.commons.lang.StringUtils;

import java.util.Properties;

public class MacroI18nBuilder
{
    private static final String MACRO_NAME_FORMAT = "%s.%s.label";
    private static final String MACRO_DESC_FORMAT = "%s.%s.desc";
    private static final String PARAM_NAME_FORMAT = "%s.%s.param.%s.label";
    private static final String PARAM_DESC_FORMAT = "%s.%s.param.%s.desc";

    private final Properties i18n;
    private final String pluginKey;

    public MacroI18nBuilder(String pluginKey)
    {
        this.i18n = new Properties();
        this.pluginKey = pluginKey;
    }

    public void add(BaseContentMacroModuleBean bean, final ConnectAddonBean addon)
    {
        addName(bean.getRawKey(), bean.getName());
        addDescription(bean.getRawKey(), bean.getDescription());

        for (MacroParameterBean parameterBean : bean.getParameters())
        {
            addParameterLabel(bean.getRawKey(), parameterBean.getIdentifier(), parameterBean.getName());
            addParameterDescription(bean.getRawKey(), parameterBean.getIdentifier(), parameterBean.getDescription());
        }
    }

    // {pluginKey}.{macroName}.label
    public MacroI18nBuilder addName(String macroKey, I18nProperty property)
    {
        if (null != property && !StringUtils.isBlank(property.getValue()))
        {
            String key = String.format(MACRO_NAME_FORMAT, pluginKey, macroKey);
            i18n.put(key, property.getValue());
        }
        return this;
    }

    // {pluginKey}.{macroName}.desc
    public MacroI18nBuilder addDescription(String macroKey, I18nProperty property)
    {
        if (null != property && !StringUtils.isBlank(property.getValue()))
        {
            String key = String.format(MACRO_DESC_FORMAT, pluginKey, macroKey);
            i18n.put(key, property.getValue());
        }
        return this;
    }

    // {pluginKey}.{macroName}.param.{paramName}.label
    public MacroI18nBuilder addParameterLabel(String macroKey, String parameterName, I18nProperty property)
    {
        if (null != property && !StringUtils.isBlank(property.getValue()))
        {
            String key = String.format(PARAM_NAME_FORMAT, pluginKey, macroKey, parameterName);
            i18n.put(key, property.getValue());
        }
        return this;
    }

    // {pluginKey}.{macroName}.param.{paramName}.desc
    public MacroI18nBuilder addParameterDescription(String macroKey, String parameterName, I18nProperty property)
    {
        if (null != property && !StringUtils.isBlank(property.getValue()))
        {
            String key = String.format(PARAM_DESC_FORMAT, pluginKey, macroKey, parameterName);
            i18n.put(key, property.getValue());
        }
        return this;
    }

    public Properties getI18nProperties()
    {
        return i18n;
    }

    public static String getMacroI18nKey(String pluginKey, String macroKey)
    {
        return String.format(MACRO_NAME_FORMAT, pluginKey, macroKey);
    }
}
