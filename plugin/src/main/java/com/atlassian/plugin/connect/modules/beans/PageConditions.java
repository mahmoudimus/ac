package com.atlassian.plugin.connect.modules.beans;

import java.util.Map;
import java.util.Set;

import com.atlassian.plugin.web.Condition;

import static com.google.common.collect.Maps.newHashMap;
import static com.google.common.collect.Maps.newTreeMap;

public class PageConditions extends ConditionsProvider
{
    public static final String CONDITION_LIST_MD = getConditionListAsMarkdown(getConditionMap());
    public static final Set<String> CONDITION_SET = getConditionMap().keySet();
    
    public static final String USER_IS_SYSADMIN = "user_is_sysadmin";
    public static final String USER_IS_LOGGED_IN = "user_is_logged_in";
    public static final String USER_IS_ADMIN = "user_is_admin";
    
    protected static Map<String, Class<? extends Condition>> getConditionMap()
    {
        Map<String, Class<? extends Condition>> conditionMap = newTreeMap();

        conditionMap.put(USER_IS_SYSADMIN, com.atlassian.plugin.connect.spi.module.UserIsSysAdminCondition.class);
        conditionMap.put(USER_IS_LOGGED_IN, com.atlassian.plugin.connect.spi.module.UserIsLoggedInCondition.class);
        conditionMap.put(USER_IS_ADMIN, com.atlassian.plugin.connect.spi.module.UserIsAdminCondition.class);
        
        return conditionMap;
    }
}
