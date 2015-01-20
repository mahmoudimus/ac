package com.atlassian.plugin.connect.plugin.ao;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;

import javax.annotation.concurrent.Immutable;

import static com.google.common.base.Preconditions.checkNotNull;


/**
 * This class represents an add-on property consisting of a key and a value.
 *
 * @since TODO: fill in the proper version before merge
 */
@Immutable
public final class AddOnProperty
{
    private final String key;
    private final String value;
    private final Long modificationTime;

    public AddOnProperty(final String key, final String value, final long modificationTime)
    {
        this.key = checkNotNull(key);
        this.value = checkNotNull(value);
        this.modificationTime = modificationTime;
    }

    public String getKey()
    {
        return key;
    }

    public String getValue()
    {
        return value;
    }

    @Override
    public boolean equals(final Object o)
    {
        if (this == o)
        {
            return true;
        }
        if (o == null || getClass() != o.getClass())
        {
            return false;
        }
        final AddOnProperty that = (AddOnProperty) o;

        return new EqualsBuilder()
                .append(key,that.key)
                .append(value, that.value)
                .append(modificationTime, that.modificationTime)
                .isEquals();
    }

    @Override
    public int hashCode()
    {
        return new HashCodeBuilder()
                .append(modificationTime)
                .hashCode();
    }

    @Override
    public String toString()
    {
        return new ToStringBuilder(this)
                .append("key", key)
                .append("value", value)
                .append("modificationTime", modificationTime)
                .toString();
    }

    public static AddOnProperty fromAO(AddOnPropertyAO ao)
    {
        return new AddOnProperty(ao.getPropertyKey(), ao.getValue(), ao.getModificationTime());
    }
}
