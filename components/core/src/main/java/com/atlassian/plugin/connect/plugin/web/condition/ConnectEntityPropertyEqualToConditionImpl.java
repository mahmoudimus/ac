package com.atlassian.plugin.connect.plugin.web.condition;

import java.util.Map;

import com.atlassian.fugue.Option;
import com.atlassian.plugin.PluginParseException;
import com.atlassian.plugin.connect.api.web.condition.ConnectEntityPropertyEqualToCondition;
import com.atlassian.plugin.connect.plugin.property.AddOnProperty;
import com.atlassian.plugin.connect.plugin.property.AddOnPropertyService;
import com.atlassian.sal.api.user.UserManager;
import com.atlassian.sal.api.user.UserProfile;

import com.google.common.base.Function;
import com.google.common.base.Strings;

import org.codehaus.jackson.JsonNode;

import static com.atlassian.plugin.connect.plugin.util.JsonCommon.parseStringToJson;

@ConnectCondition
public class ConnectEntityPropertyEqualToConditionImpl implements ConnectEntityPropertyEqualToCondition
{
    private final AddOnPropertyService addOnPropertyService;
    private final UserManager userManager;

    private String propertyKey;
    private String propertyValue;
    private String addOnKey;

    public ConnectEntityPropertyEqualToConditionImpl(final AddOnPropertyService addOnPropertyService, final UserManager userManager)
    {
        this.addOnPropertyService = addOnPropertyService;
        this.userManager = userManager;
    }

    @Override
    public void init(final Map<String, String> params) throws PluginParseException
    {
        this.propertyKey = Strings.nullToEmpty(params.get("propertyKey"));
        this.propertyValue = Strings.nullToEmpty(params.get("value"));
        Option<String> maybeAddOnKey = ConnectConditionContext.from(params).getAddOnKey();
        if (maybeAddOnKey.isEmpty())
        {
            throw new IllegalStateException("Condition should have been invoked in the Atlassian Connect context, but apparently it was not, add-on key is missing");
        }
        this.addOnKey = maybeAddOnKey.getOrNull();
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
                    System.out.println("Property value: " + propertyValue);
                    System.out.println("AddonProperty value: " + input.getValue());
                    // return propertyValue.equals(input.getValue());
                    final Option<JsonNode> propertyJson = parseStringToJson(propertyValue);
                    return propertyJson.equals(Option.some(input.getValue()));
                }
            }
        );
    }
}
