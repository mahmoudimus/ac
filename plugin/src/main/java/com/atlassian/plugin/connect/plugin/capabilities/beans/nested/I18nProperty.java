package com.atlassian.plugin.connect.plugin.capabilities.beans.nested;

/**
 * @since version
 */
public class I18nProperty
{
    private String value;
    private String i18n;

    public I18nProperty(String defaultValue,String i18n)
    {
        this.i18n = i18n;
        this.value = defaultValue;
    }

    public String getValue()
    {
        return value;
    }

    public String getI18n()
    {
        return i18n;
    }
}
