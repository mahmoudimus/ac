package com.atlassian.plugin.connect.plugin.property;

import java.util.Map;
import java.util.Optional;

import com.atlassian.plugin.PluginParseException;
import com.atlassian.plugin.connect.plugin.web.condition.ConnectCondition;
import com.atlassian.plugin.connect.plugin.web.condition.ConnectConditionContext;
import com.atlassian.plugin.web.Condition;
import com.atlassian.sal.api.user.UserManager;
import com.atlassian.sal.api.user.UserProfile;

import com.google.common.base.Function;
import com.google.common.base.Strings;

import org.codehaus.jackson.JsonNode;

import static com.atlassian.plugin.connect.plugin.property.JsonCommon.parseStringToJson;

@ConnectCondition
public class AddonEntityPropertyEqualToCondition implements Condition
{
    private final AddonPropertyService addonPropertyService;
    private final UserManager userManager;

    private String propertyKey;
    private String propertyValue;
    private String addonKey;

    public AddonEntityPropertyEqualToCondition(final AddonPropertyService addonPropertyService, final UserManager userManager)
    {
        this.addonPropertyService = addonPropertyService;
        this.userManager = userManager;
    }

    @Override
    public void init(final Map<String, String> params) throws PluginParseException
    {
        this.propertyKey = Strings.nullToEmpty(params.get("propertyKey"));
        this.propertyValue = Strings.nullToEmpty(params.get("value"));
        Optional<String> maybeAddonKey = ConnectConditionContext.from(params).getAddonKey();
        if (!maybeAddonKey.isPresent())
        {
            throw new IllegalStateException("Condition should have been invoked in the Atlassian Connect context, but apparently it was not, add-on key is missing");
        }
        this.addonKey = maybeAddonKey.get();
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
                    final Optional<JsonNode> propertyJson = parseStringToJson(propertyValue);
                    return propertyJson.equals(Optional.of(input.getValue()));
                }
            }
        );
    }
}
