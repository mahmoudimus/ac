package com.atlassian.plugin.connect.modules.beans;

import com.atlassian.json.schema.annotation.Required;
import com.atlassian.plugin.connect.modules.beans.builder.RequiredKeyBeanBuilder;
import com.google.common.base.Strings;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import static com.atlassian.plugin.connect.modules.util.ModuleKeyGenerator.cleanKey;

/**
 * @since 1.0
 */
public class RequiredKeyBean extends NamedBean
{
    /**
     * A REQUIRED key to identify this module. This key must be unique relative to the add on.
     * <p/>
     * Most modules will automatically generate a key for you. However, this module requires you to specify a key.
     * <p/>
     * All specified keys will have all special characters and spaces replaced with dashes and will be lower cased.
     * <p/>
     * example: "My Addon Key" will become "my-addon-key"
     */
    @Required
    private String key;

    private transient String calculatedKey;

    public RequiredKeyBean()
    {
        this.key = "";
    }

    public RequiredKeyBean(final RequiredKeyBeanBuilder builder)
    {
        super(builder);

        if (null == key)
        {
            this.key = "";
        }
    }

    public String getKey()
    {
        if (Strings.isNullOrEmpty(calculatedKey))
        {
            this.calculatedKey = cleanKey(key);
        }
        return calculatedKey;
    }

    public String getRawKey()
    {
        return key;
    }

    @Override
    public boolean equals(Object otherObj)
    {
        if (otherObj == this)
        {
            return true;
        }

        if (!(otherObj instanceof RequiredKeyBean && super.equals(otherObj)))
        {
            return false;
        }

        RequiredKeyBean other = (RequiredKeyBean) otherObj;

        return new EqualsBuilder()
                .append(key, other.key)
                .isEquals();
    }

    @Override
    public int hashCode()
    {
        return new HashCodeBuilder(13, 37)
                .append(super.hashCode())
                .append(key)
                .build();
    }

}
