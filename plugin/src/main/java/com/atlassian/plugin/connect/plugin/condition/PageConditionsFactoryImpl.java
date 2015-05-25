package com.atlassian.plugin.connect.plugin.condition;

import com.atlassian.plugin.connect.api.condition.ConnectEntityPropertyEqualToCondition;
import com.atlassian.plugin.connect.spi.condition.PageConditionsFactory;
import com.atlassian.plugin.connect.spi.condition.UserIsAdminCondition;
import com.atlassian.plugin.connect.spi.condition.UserIsLoggedInCondition;
import com.atlassian.plugin.connect.spi.condition.UserIsSysAdminCondition;
import com.atlassian.plugin.connect.spi.product.ConditionClassResolver;
import com.atlassian.sal.api.features.DarkFeatureEnabledCondition;
import org.springframework.stereotype.Component;

import java.util.Set;

@Component
public class PageConditionsFactoryImpl implements PageConditionsFactory
{
    public static final String FEATURE_FLAG = "feature_flag";
    public static final String USER_IS_SYSADMIN = "user_is_sysadmin";
    public static final String USER_IS_LOGGED_IN = "user_is_logged_in";
    public static final String USER_IS_ADMIN = "user_is_admin";

    private Set<String> conditionNameSet = getPageConditions().getAllConditionNames();

    @Override
    public Set<String> getConditionNames()
    {
        return conditionNameSet;
    }

    @Override
    public ConditionClassResolver getPageConditions()
    {
        return ConditionClassResolver.builder()
                .mapping(FEATURE_FLAG, DarkFeatureEnabledCondition.class)
                .mapping(USER_IS_SYSADMIN, UserIsSysAdminCondition.class)
                .mapping(USER_IS_LOGGED_IN, UserIsLoggedInCondition.class)
                .mapping(USER_IS_ADMIN, UserIsAdminCondition.class)
                .rule(ConnectEntityPropertyEqualToCondition.ENTITY_PROPERTY_EQUAL_TO, ConnectEntityPropertyEqualToCondition.RULE_PREDICATE, ConnectEntityPropertyEqualToConditionImpl.class)
                .build();
    }
}
