package com.atlassian.plugin.connect.plugin.condition;

import com.atlassian.fugue.Option;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;

import java.util.Map;
import javax.annotation.Nullable;

import static com.atlassian.fugue.Option.option;

/**
 * Wrapper over context parameters which gives access to Connect-related context parameters.
 *
 * @see com.atlassian.plugin.connect.plugin.condition.ConnectCondition
 */
public class ConnectConditionContext
{
    private static final String CONNECT_ADD_ON_KEY_KEY = "AtlassianConnectAddOnKey";

    private final Map<String, String> contextMap;

    private ConnectConditionContext(final Map<String, String> contextMap)
    {
        this.contextMap = contextMap;
    }

    public static ConnectConditionContext from(final Map<String, String> contextMap)
    {
        return new ConnectConditionContext(Maps.newHashMap(contextMap));
    }

    public static Map<String, String> addConnectContext(Map<String, String> context, String addOnKey)
    {
        Map<String, String> result = Maps.newHashMap(context);
        result.put(CONNECT_ADD_ON_KEY_KEY, addOnKey);
        return result;
    }

    public Option<String> getAddOnKey() {
        return option(contextMap.get(CONNECT_ADD_ON_KEY_KEY));
    }

    /**
     * Returns a value from the underlying map. This is equivalent to just calling {@code contextMap.get(key)}
     *
     * @param key parameter key
     * @return value or null if not defined
     */
    @Nullable
    public String get(String key) {
        return contextMap.get(key);
    }
}
