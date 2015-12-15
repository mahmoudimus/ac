package com.atlassian.plugin.connect.plugin.property;

import java.util.Optional;

import com.atlassian.plugin.connect.api.plugin.property.AddOnProperty;
import com.atlassian.plugin.connect.api.plugin.property.AddOnPropertyIterable;
import com.google.common.base.Function;
import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import org.codehaus.jackson.JsonNode;

public class AddOnPropertyFactory
{

    public static AddOnPropertyIterable fromAddOnPropertyAOList(Iterable<AddOnPropertyAO> propertyList)
    {
        return new AddOnPropertyIterable(Lists.transform(Lists.newArrayList(propertyList), new Function<AddOnPropertyAO, AddOnProperty>()
        {
            @Override
            public AddOnProperty apply(final AddOnPropertyAO propertyAO)
            {
                return fromAO(propertyAO);
            }
        }));
    }


    public static AddOnProperty fromAO(AddOnPropertyAO ao)
    {
        final Optional<JsonNode> potentialJsonValue = JsonCommon.parseStringToJson(ao.getValue());
        Preconditions.checkState(potentialJsonValue.isPresent(), String.format("The addon property %s did not contain valid json: %s", ao.getPropertyKey(), ao.getValue()));
        return new AddOnProperty(ao.getPropertyKey(), potentialJsonValue.get(), ao.getID());
    }
}
