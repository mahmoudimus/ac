package com.atlassian.plugin.connect.plugin.condition;

import com.atlassian.fugue.Option;
import com.atlassian.jira.plugin.webfragment.conditions.AbstractWebCondition;
import com.atlassian.jira.plugin.webfragment.model.JiraHelper;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.plugin.PluginParseException;
import com.atlassian.plugin.connect.plugin.condition.ConnectCondition;
import com.atlassian.plugin.connect.plugin.condition.ConnectConditionContext;
import com.atlassian.plugin.connect.plugin.property.AddOnProperty;
import com.atlassian.plugin.connect.plugin.property.AddOnPropertyService;
import com.atlassian.plugin.web.Condition;
import com.atlassian.sal.api.user.UserManager;
import com.atlassian.sal.api.user.UserProfile;
import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.base.Strings;

import java.util.Map;

@ConnectCondition
public class ConnectEntityPropertyEqualToCondition extends AbstractWebCondition implements Condition
{
    public static final Predicate<Map<String, String>> RULE_PREDICATE = new Predicate<Map<String, String>>()
    {
        @Override
        public boolean apply(final Map<String, String> parameters)
        {
            return "addon".equals(parameters.get("entity"));
        }
    };

    public static final String ENTITY_PROPERTY_EQUAL_TO = "entity_property_equal_to";

    private final AddOnPropertyService addOnPropertyService;
    private final UserManager userManager;

    private String propertyKey;
    private String propertyValue;
    private String addOnKey;

    public ConnectEntityPropertyEqualToCondition(final AddOnPropertyService addOnPropertyService, final UserManager userManager)
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
    public boolean shouldDisplay(final ApplicationUser applicationUser, final JiraHelper jiraHelper)
    {
        UserProfile userProfile = applicationUser != null ? userManager.getUserProfile(applicationUser.getUsername()) : null;
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
                        return propertyValue.equals(input.getValue());
                    }
                }
        );
    }
}
