package com.atlassian.plugin.connect.plugin.ao;

import com.atlassian.plugin.connect.plugin.rest.data.ETag;
import com.google.common.hash.Hashing;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;

import javax.annotation.concurrent.Immutable;

import static com.google.gson.internal.$Gson$Preconditions.checkNotNull;

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

    public AddOnProperty(final String key, final String value)
    {
        this.key = checkNotNull(key);
        this.value = checkNotNull(value);
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
                .isEquals();
    }

    @Override
    public int hashCode()
    {
        return new HashCodeBuilder()
                .append(key)
                .append(value)
                .hashCode();
    }

    @Override
    public String toString()
    {
        return new ToStringBuilder(this)
                .append("key", key)
                .append("value", value)
                .toString();
    }

    public ETag getETag()
    {
        return new ETag(Hashing.md5().hashString(value).toString());
    }

    public static AddOnProperty fromAO(AddOnPropertyAO ao)
    {
        return new AddOnProperty(ao.getPropertyKey(), ao.getValue());
    }
}
