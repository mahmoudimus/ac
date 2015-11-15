package com.atlassian.plugin.connect.plugin.property;

import com.google.common.base.Function;
import com.google.common.base.Preconditions;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.codehaus.jackson.JsonNode;

import java.util.Iterator;

import com.atlassian.fugue.Option;
import com.atlassian.plugin.connect.plugin.util.JsonCommon;

/**
 * This class represents an add-on property iterable which consist of a key and a value.
 */
public class AddOnPropertyIterable implements Iterable<AddOnProperty>
{
    private final Iterable<AddOnProperty> properties;

    public AddOnPropertyIterable(final Iterable<AddOnProperty> properties)
    {
        this.properties = properties;
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
                final Option<JsonNode> potentialValue = JsonCommon.parseStringToJson(propertyAO.getValue());
                Preconditions.checkState(potentialValue.isDefined(), String.format("The value being read out of the property %s was not valid JSON: %s", propertyAO.getPropertyKey(), propertyAO.getValue()));
                return new AddOnProperty(propertyAO.getPropertyKey(), potentialValue.get(), propertyAO.getID());
            }
        }));
    }
}