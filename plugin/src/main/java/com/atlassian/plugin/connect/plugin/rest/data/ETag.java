package com.atlassian.plugin.connect.plugin.rest.data;

import com.atlassian.fugue.Option;
import com.google.common.base.Suppliers;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

/**
 * TODO: Document this class / interface here
 *
 * @since v6.3
 */
public class ETag
{
    private final Option<String> eTag;

    private ETag()
    {
        eTag = Option.none();
    }

    public ETag(final String eTag)
    {
        this.eTag = Option.option(eTag);
    }

    public boolean isDefined()
    {
        return eTag.isDefined();
    }

    public static ETag emptyETag()
    {
        return new ETag();
    }

    @Override
    public int hashCode() { return new HashCodeBuilder().append(this.eTag).toHashCode();}

    @Override
    public boolean equals(final Object obj)
    {
        if (obj == null) {return false;}
        if (getClass() != obj.getClass()) {return false;}
        final ETag other = (ETag) obj;
        return new EqualsBuilder().append(this.eTag, other.eTag).isEquals();
    }

    @Override
    public String toString()
    {
        return eTag.getOrElse(Suppliers.ofInstance(""));
    }
}
