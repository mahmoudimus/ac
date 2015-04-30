package com.atlassian.plugin.connect.plugin.condition;

import com.atlassian.plugin.connect.spi.condition.UserIsAdminCondition;
import com.atlassian.plugin.connect.spi.condition.UserIsLoggedInCondition;
import com.atlassian.plugin.connect.spi.condition.UserIsSysAdminCondition;
import com.atlassian.plugin.connect.spi.product.ConditionClassResolver;
import com.atlassian.sal.api.features.DarkFeatureEnabledCondition;

import java.util.Set;

public abstract class PageConditions extends ConditionsProvider
{
    @SuppressWarnings ("UnusedDeclaration")
    public static final String CONDITION_LIST_MD = getConditionListAsMarkdown(getConditionMap());

    public static final Set<String> CONDITION_SET = getConditionMap().getAllConditionNames();

    public static final String FEATURE_FLAG = "feature_flag";
    public static final String USER_IS_SYSADMIN = "user_is_sysadmin";
    public static final String USER_IS_LOGGED_IN = "user_is_logged_in";
    public static final String USER_IS_ADMIN = "user_is_admin";

    public PageConditions(ConditionClassResolver conditions)
    {
        super(conditions);
    }

    protected static ConditionClassResolver getConditionMap()
    {
        return ConditionClassResolver.builder()
                .mapping(FEATURE_FLAG, DarkFeatureEnabledCondition.class)
                .mapping(USER_IS_SYSADMIN, UserIsSysAdminCondition.class)
                .mapping(USER_IS_LOGGED_IN, UserIsLoggedInCondition.class)
                .mapping(USER_IS_ADMIN, UserIsAdminCondition.class)
                .build();
    }
}
