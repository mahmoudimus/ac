package com.atlassian.plugin.connect.plugin.property;

import java.util.Optional;

import com.atlassian.plugin.connect.api.property.AddonProperty;
import com.atlassian.plugin.connect.api.property.AddonPropertyIterable;
import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import org.codehaus.jackson.JsonNode;

public class AddonPropertyFactory {

    public static AddonPropertyIterable fromAddonPropertyAOList(Iterable<AddonPropertyAO> propertyList) {
        return new AddonPropertyIterable(Lists.transform(Lists.newArrayList(propertyList), AddonPropertyFactory::fromAO));
    }

    public static AddonProperty fromAO(AddonPropertyAO ao) {
        final Optional<JsonNode> potentialJsonValue = JsonCommon.parseStringToJson(ao.getValue());
        Preconditions.checkState(potentialJsonValue.isPresent(), String.format("The addon property %s did not contain valid json: %s", ao.getPropertyKey(), ao.getValue()));
        return new AddonProperty(ao.getPropertyKey(), potentialJsonValue.get(), ao.getID());
    }
}
