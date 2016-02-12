package com.atlassian.plugin.connect.api.property;

import java.util.Iterator;

import com.atlassian.annotations.PublicApi;
import com.google.common.collect.Iterables;
import org.apache.commons.lang.builder.HashCodeBuilder;

/**
 * This class represents an add-on property iterable which consist of a key and a value.
 */
@PublicApi
public class AddonPropertyIterable implements Iterable<AddonProperty>
{
    private final Iterable<AddonProperty> properties;

    public AddonPropertyIterable(final Iterable<AddonProperty> properties)
    {
        this.properties = properties;
    }

    @Override
    public Iterator<AddonProperty> iterator()
    {
        return properties.iterator();
    }

    public Iterable<String> getPropertyKeys()
    {
        return Iterables.transform(properties, AddonProperty::getKey);
    }

    @Override
    public int hashCode()
    {
        return new HashCodeBuilder().append(this.properties).toHashCode();
    }

    @Override
    public boolean equals(final Object obj)
    {
        if (obj == null) {return false;}
        if (getClass() != obj.getClass()) {return false;}
        final AddonPropertyIterable other = (AddonPropertyIterable) obj;

        return Iterables.elementsEqual(properties, other.properties);
    }
}
