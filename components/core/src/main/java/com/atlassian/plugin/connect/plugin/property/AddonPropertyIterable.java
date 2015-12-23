package com.atlassian.plugin.connect.plugin.property;

import java.util.Iterator;
import java.util.Optional;

import com.google.common.base.Function;
import com.google.common.base.Preconditions;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

import org.apache.commons.lang.builder.HashCodeBuilder;
import org.codehaus.jackson.JsonNode;

/**
 * This class represents an add-on property iterable which consist of a key and a value.
 */
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
        return Iterables.transform(properties, new Function<AddonProperty, String>()
        {
            @Override
            public String apply(final AddonProperty property)
            {
                return property.getKey();
            }
        });
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

    public static AddonPropertyIterable fromAddonPropertyAOList(Iterable<AddonPropertyAO> propertyList)
    {
        return new AddonPropertyIterable(Lists.transform(Lists.newArrayList(propertyList), new Function<AddonPropertyAO, AddonProperty>()
        {
            @Override
            public AddonProperty apply(final AddonPropertyAO propertyAO)
            {
                return AddonProperty.fromAO(propertyAO);
            }
        }));
    }
}
