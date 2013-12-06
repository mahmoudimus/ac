package com.atlassian.plugin.connect.plugin.capabilities.beans.nested;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

/**
 * @since 1.0
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

    public static I18nProperty empty()
    {
        return new I18nProperty("", "");
    }

    @Override
    public boolean equals(Object otherObj)
    {
        if (otherObj == this)
        {
            return true;
        }

        if (!(otherObj instanceof I18nProperty))
        {
            return false;
        }

        I18nProperty other = (I18nProperty) otherObj;

        return new EqualsBuilder()
                .append(value, other.value)
                .append(i18n, other.i18n)
                .isEquals();
    }

    @Override
    public int hashCode()
    {
        return new HashCodeBuilder(61, 11)
                .append(value)
                .append(i18n)
                .build();
    }
}
