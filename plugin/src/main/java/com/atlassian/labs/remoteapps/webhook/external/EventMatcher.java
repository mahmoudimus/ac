package com.atlassian.labs.remoteapps.webhook.external;

import com.atlassian.applinks.api.ApplicationLink;

/**
 * Matches an event for publication to web hook listeners
 */
public interface EventMatcher<T>
{
    EventMatcher<Object> ALWAYS_TRUE = new EventMatcher<Object>() {
        @Override
        public boolean matches(Object event, String pluginKey)
        {
            return true;
        }
    };

    /**
     * Useful for registering events but then firing them manually outside the event system
     */
    EventMatcher<Object> ALWAYS_FALSE = new EventMatcher<Object>() {
            @Override
            public boolean matches(Object event, String pluginKey)
            {
                return false;
            }
        };

    boolean matches(T event, String pluginKey);
}
