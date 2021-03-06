package com.atlassian.plugin.connect.api.web.condition;

import com.google.common.collect.Maps;

import javax.annotation.Nullable;
import java.util.Map;
import java.util.Optional;

import static java.util.Optional.ofNullable;

/**
 * Wrapper over context parameters which gives access to Connect-related context parameters.
 *
 * @see ConnectCondition
 */
public class ConnectConditionContext {
    public static final String CONNECT_ADD_ON_KEY_KEY = "addonKey";

    private final Map<String, String> contextMap;

    private ConnectConditionContext(final Map<String, String> contextMap) {
        this.contextMap = contextMap;
    }

    public static ConnectConditionContext from(final Map<String, String> contextMap) {
        return new ConnectConditionContext(Maps.newHashMap(contextMap));
    }

    public static Builder builder(Map<String, String> initialState) {
        return new Builder(initialState);
    }

    public Optional<String> getAddonKey() {
        return ofNullable(contextMap.get(CONNECT_ADD_ON_KEY_KEY));
    }

    public Map<String, String> toMap() {
        return Maps.newHashMap(contextMap);
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


    public static final class Builder {

        private final Map<String, String> accumulator;

        private Builder(final Map<String, String> initialState) {
            this.accumulator = Maps.newHashMap(initialState);
        }

        public Builder putAddonKey(String addonKey) {
            return this.put(CONNECT_ADD_ON_KEY_KEY, addonKey);
        }

        public Builder put(String key, String value) {
            accumulator.put(key, value);
            return this;
        }

        public ConnectConditionContext build() {
            return ConnectConditionContext.from(accumulator);
        }

    }
}
