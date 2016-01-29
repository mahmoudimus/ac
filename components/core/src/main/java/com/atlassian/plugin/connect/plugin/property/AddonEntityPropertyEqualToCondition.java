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
    private String propertyValue;
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

        this.propertyKey = Strings.nullToEmpty(params.get("propertyKey"));
        this.propertyValue = Strings.nullToEmpty(params.get("value"));
        this.jsonPath = Strings.nullToEmpty(params.get("objectName"));
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
                    // Get the expected value
                    final Optional<JsonNode> potentialExpectedValue = parseStringToJson(propertyValue);
                    if(!potentialExpectedValue.isPresent()) return false;
                    final JsonNode expectedValue = potentialExpectedValue.get();

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
