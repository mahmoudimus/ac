package com.atlassian.plugin.connect.plugin.property;

import java.util.Map;
import java.util.Optional;

import com.atlassian.plugin.PluginParseException;
import com.atlassian.plugin.connect.api.property.AddonPropertyService;
import com.atlassian.plugin.connect.api.web.condition.AbstractConnectCondition;
import com.atlassian.sal.api.user.UserManager;
import com.atlassian.sal.api.user.UserProfile;
import com.google.common.base.Strings;
import org.codehaus.jackson.JsonNode;

import static com.atlassian.plugin.connect.plugin.property.JsonCommon.parseStringToJson;

public class AddonEntityPropertyEqualToCondition extends AbstractConnectCondition
{
    private final AddonPropertyService addonPropertyService;
    private final UserManager userManager;

    private String propertyKey;
    private String propertyValue;

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
    }

    @Override
    public boolean shouldDisplay(final Map<String, Object> context)
    {
        UserProfile userProfile = userManager.getUserProfile(userManager.getRemoteUserKey());
        return addonPropertyService.getPropertyValue(userProfile, addonKey, addonKey, propertyKey).fold(
                input -> false,
                input -> {
                    final Optional<JsonNode> propertyJson = parseStringToJson(propertyValue);
                    return propertyJson.equals(Optional.of(input.getValue()));
                }
        );
    }
}
