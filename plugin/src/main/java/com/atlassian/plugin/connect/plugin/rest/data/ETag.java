package com.atlassian.plugin.connect.plugin.rest.data;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

/**
 * Class representing an entity tag used to identify a resource
 *
 * @since TODO: fill in the proper version before merge
 */
public final class ETag
{
    private final String hash;

    public ETag(final String hash)
    {
        this.hash = hash;
    }

    @Override
    public int hashCode() { return new HashCodeBuilder().append(this.hash).toHashCode();}

    @Override
    public boolean equals(final Object obj)
    {
        if (obj == null) {return false;}
        if (getClass() != obj.getClass()) {return false;}
        final ETag other = (ETag) obj;
        return new EqualsBuilder().append(this.hash, other.hash).isEquals();
    }

    @Override
    public String toString()
    {
        return hash;
    }
}
