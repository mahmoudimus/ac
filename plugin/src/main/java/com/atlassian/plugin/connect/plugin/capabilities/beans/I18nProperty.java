package com.atlassian.plugin.connect.plugin.capabilities.beans;

/**
 * @since version
 */
public class I18nProperty
{
    private String defaultValue;
    private String i18n;

    public I18nProperty(String i18n, String defaultValue)
    {
        this.i18n = i18n;
        this.defaultValue = defaultValue;
    }

    public String getDefaultValue()
    {
        return defaultValue;
    }

    public String getI18n()
    {
        return i18n;
    }
}
