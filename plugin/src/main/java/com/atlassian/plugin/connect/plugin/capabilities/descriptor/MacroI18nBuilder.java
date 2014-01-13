package com.atlassian.plugin.connect.plugin.capabilities.descriptor;

import com.atlassian.plugin.connect.modules.beans.nested.I18nProperty;
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
    private final String macroName;

    public MacroI18nBuilder(String pluginKey, String macroName)
    {
        this.i18n = new Properties();
        this.pluginKey = pluginKey;
        this.macroName = macroName;
    }

    // {pluginKey}.{macroName}.label
    public MacroI18nBuilder addName(I18nProperty property)
    {
        if (!StringUtils.isBlank(property.getValue()))
        {
            String key = String.format(MACRO_NAME_FORMAT, pluginKey, macroName);
            i18n.put(key, property.getValue());
        }
        return this;
    }

    // {pluginKey}.{macroName}.desc
    public MacroI18nBuilder addDescription(I18nProperty property)
    {
        if (!StringUtils.isBlank(property.getValue()))
        {
            String key = String.format(MACRO_DESC_FORMAT, pluginKey, macroName);
            i18n.put(key, property.getValue());
        }
        return this;
    }

    // {pluginKey}.{macroName}.param.{paramName}.label
    public MacroI18nBuilder addParameterLabel(String parameterName, I18nProperty property)
    {
        if (!StringUtils.isBlank(property.getValue()))
        {
            String key = String.format(PARAM_NAME_FORMAT, pluginKey, macroName, parameterName);
            i18n.put(key, property.getValue());
        }
        return this;
    }

    // {pluginKey}.{macroName}.param.{paramName}.desc
    public MacroI18nBuilder addParameterDescription(String parameterName, I18nProperty property)
    {
        if (!StringUtils.isBlank(property.getValue()))
        {
            String key = String.format(PARAM_DESC_FORMAT, pluginKey, macroName, parameterName);
            i18n.put(key, property.getValue());
        }
        return this;
    }

    public Properties getI18nProperties()
    {
        return i18n;
    }

    public static String getMacroI18nKey(String pluginKey, String macroName)
    {
        return String.format(MACRO_NAME_FORMAT, pluginKey, macroName);
    }
}
