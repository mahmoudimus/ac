package com.atlassian.plugin.connect.modules.beans.nested;

import com.atlassian.json.schema.annotation.Required;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

/**
 * Represents a string that can be resolved via a localization properties file.
 * <p/>
 * #### Example
 *
 * @exampleJson {@see com.atlassian.plugin.connect.modules.beans.ConnectJsonExamples#I18N_EXAMPLE}
 * @schemaTitle i18n Property
 * @since 1.0
 */
public class I18nProperty
{
    /**
     * The human-readable value
     */
    @Required
    private String value;

    /**
     * The localization key for the human-readable value.
     * If this key is provided, it will be looked up in an I18n properties file to get the value.
     */
    private String i18n;

    public I18nProperty(String defaultValue, String i18n)
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
