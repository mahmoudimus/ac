package com.atlassian.plugin.connect.plugin.ao;

import com.google.common.base.Function;
import com.google.common.collect.Iterables;
import com.google.common.hash.HashCode;
import com.google.common.hash.Hashing;

import java.util.Iterator;
import java.util.List;
import javax.annotation.Nullable;

/**
 * This class represents an add-on property list which consist of a key and a value.
 *
 * @since TODO: fill in the proper version before merge
 */
public class AddOnPropertyIterable implements Iterable<AddOnProperty>
{
    final Iterable<AddOnProperty> properties;

    public AddOnPropertyIterable(final Iterable<AddOnProperty> properties)
    {
        this.properties = properties;
    }

    @Override
    public Iterator<AddOnProperty> iterator()
    {
        return properties.iterator();
    }

    public Iterable<String> getPropertyKeysIterable()
    {
        return Iterables.transform(properties, new Function<AddOnProperty, String>()
        {
            @Override
            public String apply(@Nullable final AddOnProperty input)
            {
                return input.getKey();
            }
        });
    }

    public static AddOnPropertyIterable fromAddOnPropertyAOList(List<AddOnPropertyAO> propertyList)
    {
        return new AddOnPropertyIterable(Iterables.transform(propertyList, new Function<AddOnPropertyAO, AddOnProperty>()
        {
            @Override
            public AddOnProperty apply(@Nullable final AddOnPropertyAO input)
            {
                return new AddOnProperty(input.getPropertyKey(), input.getValue());
            }
        }));
    }
}