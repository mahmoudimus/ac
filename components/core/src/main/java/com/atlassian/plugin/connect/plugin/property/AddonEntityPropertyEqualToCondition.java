package com.atlassian.plugin.connect.plugin.property;

import java.util.Map;
import java.util.Optional;

import com.atlassian.plugin.PluginParseException;
import com.atlassian.plugin.connect.api.plugin.property.AddOnProperty;
import com.atlassian.plugin.connect.api.plugin.property.AddOnPropertyService;
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
    private final AddOnPropertyService addOnPropertyService;
    private final UserManager userManager;

    private String propertyKey;
    private String propertyValue;
    private String addOnKey;

    public AddonEntityPropertyEqualToCondition(final AddOnPropertyService addOnPropertyService, final UserManager userManager)
    {
        this.addOnPropertyService = addOnPropertyService;
        this.userManager = userManager;
    }

    @Override
    public void init(final Map<String, String> params) throws PluginParseException
    {
        this.propertyKey = Strings.nullToEmpty(params.get("propertyKey"));
        this.propertyValue = Strings.nullToEmpty(params.get("value"));
        Optional<String> maybeAddOnKey = ConnectConditionContext.from(params).getAddOnKey();
        if (!maybeAddOnKey.isPresent())
        {
            throw new IllegalStateException("Condition should have been invoked in the Atlassian Connect context, but apparently it was not, add-on key is missing");
        }
        this.addOnKey = maybeAddOnKey.get();
    }

    @Override
    public boolean shouldDisplay(final Map<String, Object> context)
    {
        UserProfile userProfile = userManager.getUserProfile(userManager.getRemoteUserKey());
        return addOnPropertyService.getPropertyValue(userProfile, addOnKey, addOnKey, propertyKey).fold(
            new Function<AddOnPropertyService.OperationStatus, Boolean>()
            {
                @Override
                public Boolean apply(final AddOnPropertyService.OperationStatus input)
                {
                    return false;
                }
            }, new Function<AddOnProperty, Boolean>()
            {
                @Override
                public Boolean apply(final AddOnProperty input)
                {
                    final Optional<JsonNode> propertyJson = parseStringToJson(propertyValue);
                    return propertyJson.equals(Optional.of(input.getValue()));
                }
            }
        );
    }
}
