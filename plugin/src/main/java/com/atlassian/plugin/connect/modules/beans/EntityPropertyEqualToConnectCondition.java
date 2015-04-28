package com.atlassian.plugin.connect.modules.beans;

import com.atlassian.fugue.Function2;
import com.atlassian.fugue.Option;
import com.atlassian.jira.plugin.webfragment.conditions.AbstractWebCondition;
import com.atlassian.jira.plugin.webfragment.conditions.EntityPropertyEqualToCondition;
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
import com.google.common.base.Strings;

import java.util.Map;

@ConnectCondition
public class EntityPropertyEqualToConnectCondition extends AbstractWebCondition implements Condition
{
    private final EntityPropertyEqualToCondition entityPropertyEqualToCondition;
    private final AddOnPropertyService addOnPropertyService;
    private final UserManager userManager;

    private Function2<ApplicationUser, JiraHelper, Boolean> shouldDisplayFunction;

    public EntityPropertyEqualToConnectCondition(EntityPropertyEqualToCondition entityPropertyEqualToCondition, final AddOnPropertyService addOnPropertyService, final UserManager userManager)
    {
        this.entityPropertyEqualToCondition = entityPropertyEqualToCondition;
        this.addOnPropertyService = addOnPropertyService;
        this.userManager = userManager;
    }

    @Override
    public void init(final Map<String, String> params) throws PluginParseException
    {
        if ("addOn".equals(params.get("entity")))
        {
            final String propertyKey = Strings.nullToEmpty(params.get("propertyKey"));
            final String propertyValue = Strings.nullToEmpty(params.get("value"));
            final ConnectConditionContext connectContext = ConnectConditionContext.from(params);
            shouldDisplayFunction = new Function2<ApplicationUser, JiraHelper, Boolean>()
            {
                @Override
                public Boolean apply(final ApplicationUser applicationUser, final JiraHelper jiraHelper)
                {
                    return shouldDisplayBasedOnAddOnProperty(applicationUser, propertyKey, propertyValue, connectContext);
                }
            };
        }
        else
        {
            entityPropertyEqualToCondition.init(params);
            shouldDisplayFunction = new Function2<ApplicationUser, JiraHelper, Boolean>()
            {
                @Override
                public Boolean apply(final ApplicationUser applicationUser, final JiraHelper jiraHelper)
                {
                    return entityPropertyEqualToCondition.shouldDisplay(applicationUser, jiraHelper);
                }
            };
        }
    }

    @Override
    public boolean shouldDisplay(final ApplicationUser applicationUser, final JiraHelper jiraHelper)
    {
        return shouldDisplayFunction.apply(applicationUser, jiraHelper);
    }

    private Boolean shouldDisplayBasedOnAddOnProperty(final ApplicationUser applicationUser, final String propertyKey, final String propertyValue, final ConnectConditionContext context)
    {
        Option<String> addOnKey = context.getAddOnKey();
        UserProfile userProfile = applicationUser != null ? userManager.getUserProfile(applicationUser.getUsername()) : null;
        if (addOnKey.isDefined())
        {
            return addOnPropertyService.getPropertyValue(userProfile, addOnKey.get(), addOnKey.get(), propertyKey).fold(
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
        else
        {
            throw new IllegalStateException("Condition should have been invoked in the Atlassian Connect context, but apparently it was not, add-on key is missing");
        }
    }
}
