package com.atlassian.plugin.connect.plugin.ao;

import com.atlassian.plugin.connect.plugin.rest.data.ETag;
import com.google.common.base.Function;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import org.apache.commons.lang.builder.HashCodeBuilder;
import com.google.common.hash.HashCode;
import com.google.common.hash.HashFunction;
import com.google.common.hash.Hashing;

import java.util.Iterator;

/**
 * This class represents an add-on property list which consist of a key and a value.
 *
 * @since TODO: fill in the proper version before merge
 */
public class AddOnPropertyIterable implements Iterable<AddOnProperty>
{
    private final Iterable<AddOnProperty> properties;

    public AddOnPropertyIterable(final Iterable<AddOnProperty> properties)
    {
        this.properties = properties;
    }

    public ETag getETag()
    {
        final HashFunction hashFunction = Hashing.md5();
        HashCode hashCode = Hashing.combineOrdered(Iterables.transform(properties, new Function<AddOnProperty, HashCode>()
        {
            @Override
            public HashCode apply(final AddOnProperty input)
            {
                return hashFunction.hashString(input.getValue());
            }
        }));
        return new ETag(hashCode.toString());
    }

    @Override
    public Iterator<AddOnProperty> iterator()
    {
        return properties.iterator();
    }

    public Iterable<String> getPropertyKeys()
    {
        return Iterables.transform(properties, new Function<AddOnProperty, String>()
        {
            @Override
            public String apply(final AddOnProperty property)
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
        final AddOnPropertyIterable other = (AddOnPropertyIterable) obj;

        return Iterables.elementsEqual(properties, other.properties);
    }

    public static AddOnPropertyIterable fromAddOnPropertyAOList(Iterable<AddOnPropertyAO> propertyList)
    {
        return new AddOnPropertyIterable(Lists.transform(Lists.newArrayList(propertyList), new Function<AddOnPropertyAO, AddOnProperty>()
        {
            @Override
            public AddOnProperty apply(final AddOnPropertyAO propertyAO)
            {
                return new AddOnProperty(propertyAO.getPropertyKey(), propertyAO.getValue());
            }
        }));
    }
}