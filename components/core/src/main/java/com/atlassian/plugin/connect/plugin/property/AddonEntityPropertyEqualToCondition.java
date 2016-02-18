package com.atlassian.plugin.connect.plugin.property;

import java.util.Map;
import java.util.Optional;

import com.atlassian.plugin.PluginParseException;
import com.atlassian.plugin.connect.api.property.AddonPropertyService;
import com.atlassian.plugin.connect.api.web.condition.AbstractConnectCondition;
import com.atlassian.sal.api.user.UserManager;
import com.atlassian.sal.api.user.UserProfile;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Strings;

import org.apache.commons.lang3.StringUtils;
import org.codehaus.jackson.JsonNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.atlassian.plugin.connect.plugin.property.JsonCommon.parseStringToJson;

public class AddonEntityPropertyEqualToCondition extends AbstractConnectCondition {
    private static final Logger log = LoggerFactory.getLogger(AddonEntityPropertyEqualToCondition.class);

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
        validateObjectNameHasCorrectSyntax(this.jsonPath);
    }

    private static String getRequiredProperty(Map<String, String> params, String paramName) {
        final String property = params.get(paramName);
        if(property == null) throw new PluginParseException("Add-on entity_property_equal_to condition is missing parameter: " + paramName);
        return property;
    }

    private void validateObjectNameHasCorrectSyntax(final String objectName) {
        if(StringUtils.isNotBlank(objectName)) {
            final Optional<String> potentialError = validateObjectName(objectName);
            if(potentialError.isPresent()) {
                throw new PluginParseException("Failed to read the objectName property: " + potentialError.get() + " (" + objectName + ")");
            }
        }
    }

    private static Optional<String> validateObjectName(String objectName) {
        if(objectName.contains("..")) return Optional.of("Two '.' characters in a row is not valid in an objectName");

        return Optional.empty();
    }

    @Override
    public boolean shouldDisplay(final Map<String, Object> context)
    {
        UserProfile userProfile = userManager.getUserProfile(userManager.getRemoteUserKey());
        return addonPropertyService.getPropertyValue(userProfile, addonKey, addonKey, propertyKey).fold(
            input -> false,
            input -> {
                // Load the actual value
                JsonNode actualValue = input.getValue();

                // If an objectName has been specified then attempt to extract that node
                if (StringUtils.isNotBlank(jsonPath))
                {
                    final Optional<JsonNode> potentialValue = getValueForPath(actualValue, jsonPath);
                    if (!potentialValue.isPresent()) return false;
                    actualValue = potentialValue.get();
                }

                if(expectedValue.equals(actualValue)) return true;

                // All of the code below this point in this method should be deleted by https://ecosystem.atlassian.net/browse/ACJIRA-825
                // In the past this condition worked purely on string comparison and therefore boolean values and numerical values
                // would successfully compare against their string equivalents.
                if(StringUtils.isNotBlank(jsonPath)) return false;
                if (!expectedValue.isValueNode() || !actualValue.isValueNode()) return false;
                final boolean stringComparisonEqual = actualValue.isTextual() && expectedValue.asText().equals(
                    actualValue.getTextValue());
                if (stringComparisonEqual) {
                    log.warn("Deprecation Warning: entity_property_equal_to condition was not equivalent by JSON comparison but was by String comparison. "
                        + "Please ensure that the entity property values and the condition 'value' are of the same json type. "
                        + "Type coercion has made the actual value: '" + actualValue + "' be equivalent to the expected value"
                        + "'" + expectedValue + "'. Please update the expected 'value' in the entity property condition"
                        + "to match the actual value. This condition was defined in the add-on: " + addonKey);
                }
                return stringComparisonEqual;
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
