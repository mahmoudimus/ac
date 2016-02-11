package com.atlassian.plugin.connect.plugin.web.condition;

import com.atlassian.plugin.connect.api.web.condition.UserIsAdminCondition;
import com.atlassian.plugin.connect.modules.beans.ConnectPageModuleBean;
import com.atlassian.plugin.connect.spi.web.condition.ConnectConditionClassResolver;
import com.google.common.collect.ImmutableList;

import java.util.List;

import static com.atlassian.plugin.connect.spi.web.condition.ConnectConditionClassResolver.Entry.newEntry;

/**
 * @see ConnectPageModuleBean#conditions documentation of context-free conditions
 */
public class CommonConditionClassResolver implements ConnectConditionClassResolver
{

    @Override
    public List<Entry> getEntries()
    {
        return ImmutableList.of(
                newEntry("feature_flag", com.atlassian.sal.api.features.DarkFeatureEnabledCondition.class).contextFree().build(),
                newEntry("user_is_sysadmin", UserIsSysAdminCondition.class).contextFree().build(),
                newEntry("user_is_logged_in", UserIsLoggedInCondition.class).contextFree().build(),
                newEntry("user_is_admin", UserIsAdminCondition.class).contextFree().build(),
                newEntry(UserIsInGroupCondition.CONDITION_NAME, UserIsInGroupCondition.class).contextFree().build(),
                newEntry("addon_is_licensed", IsLicensedCondition.class).contextFree().build()
        );
    }
}
