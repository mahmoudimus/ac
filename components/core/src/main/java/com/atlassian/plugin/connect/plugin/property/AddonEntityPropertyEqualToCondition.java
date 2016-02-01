package com.atlassian.plugin.connect.plugin.property;

import java.util.Map;
import java.util.Optional;

import com.atlassian.plugin.PluginParseException;
import com.atlassian.plugin.connect.api.web.condition.AbstractConnectCondition;
import com.atlassian.sal.api.user.UserManager;
import com.atlassian.sal.api.user.UserProfile;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Function;
import com.google.common.base.Strings;

import org.apache.commons.lang3.StringUtils;
import org.codehaus.jackson.JsonNode;

import static com.atlassian.plugin.connect.plugin.property.JsonCommon.parseStringToJson;

public class AddonEntityPropertyEqualToCondition extends AbstractConnectCondition
{
    private final AddonPropertyService addonPropertyService;
    private final UserManager userManager;

    private String propertyKey;
    private JsonNode expectedValue;
    private String jsonPath;

    public AddonEntityPropertyEqualToCondition(final AddonPropertyService addonPropertyService, final UserManager userManager)
    {
        this.addonPropertyService = addonPropertyService;
        this.userManager = userManager;
    }

    @Override
    public void init(final Map<String, String> params) throws PluginParseException
    {
        super.init(params);

        // Required values
        this.propertyKey = getRequiredProperty(params, "propertyKey");
        final String rawValue = getRequiredProperty(params, "value");

        // Validate the required values
        final Optional<JsonNode> potentialValue = parseStringToJson(rawValue);
        if(!potentialValue.isPresent()) throw new PluginParseException("The 'value' of the add-on entity_property_equal_to condition could not be parsed into JSON. Please check that it is valid json: " + rawValue);
        this.expectedValue = potentialValue.get();

        // Optional values
        this.jsonPath = Strings.nullToEmpty(params.get("objectName"));
    }

    private static String getRequiredProperty(Map<String, String> params, String paramName) {
        final String property = params.get(paramName);
        if(property == null) throw new PluginParseException("Add-on entity_property_equal_to condition is missing parameter: " + paramName);
        return property;
    }

    @Override
    public boolean shouldDisplay(final Map<String, Object> context)
    {
        UserProfile userProfile = userManager.getUserProfile(userManager.getRemoteUserKey());
        return addonPropertyService.getPropertyValue(userProfile, addonKey, addonKey, propertyKey).fold(
            new Function<AddonPropertyService.OperationStatus, Boolean>()
            {
                @Override
                public Boolean apply(final AddonPropertyService.OperationStatus input)
                {
                    return false;
                }
            }, new Function<AddonProperty, Boolean>()
            {
                @Override
                public Boolean apply(final AddonProperty input)
                {
                    // Load the actual value
                    JsonNode actualValue = input.getValue();

                    // If an objectName has been specified then attempt to extract that node
                    if(StringUtils.isNotBlank(jsonPath)) {
                        final Optional<JsonNode> potentialValue = getValueForPath(actualValue, jsonPath);
                        if(!potentialValue.isPresent()) return false;
                        actualValue = potentialValue.get();
                    }

                    return expectedValue.equals(actualValue);
                }
            }
        );
    }

    @VisibleForTesting
    static Optional<JsonNode> getValueForPath(final JsonNode jsonEntityProperty, final String path) {
        final String[] split = StringUtils.split(path, '.');
        JsonNode value = jsonEntityProperty;
        for (final String currentKey : split) {
            if (value == null || !value.isObject()) {
                return Optional.empty();
            } else {
                value = value.get(currentKey);
            }
        }
        return Optional.ofNullable(value);
    }
}
