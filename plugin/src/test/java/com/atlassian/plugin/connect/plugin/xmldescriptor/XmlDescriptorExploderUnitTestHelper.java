package com.atlassian.plugin.connect.plugin.xmldescriptor;

import com.atlassian.event.api.EventPublisher;
import com.atlassian.sal.api.features.DarkFeatureManager;
import com.atlassian.sal.api.features.EnabledDarkFeatures;
import com.atlassian.sal.api.user.UserKey;

import javax.annotation.Nullable;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Instantiate a {@link com.atlassian.plugin.connect.plugin.xmldescriptor.XmlDescriptorExploder} for unit tests.
 * We need an instance so that it doesn't throw an exception complaining that
 * "A XmlDescriptorExploder instance must be constructed before notifyAndExplode() is called".
 */
public class XmlDescriptorExploderUnitTestHelper
{
    private static final AtomicBoolean isInitialised = new AtomicBoolean(false);

    public static void runBeforeTests()
    {
        if (!isInitialised.getAndSet(true))
        {
            new XmlDescriptorExploder(createDarkFeatureManager(), createEventPublisher());
        }
    }

    private static DarkFeatureManager createDarkFeatureManager()
    {
        return new DarkFeatureManager()
        {
            @Override
            public boolean isFeatureEnabledForAllUsers(String featureKey)
            {
                return false;
            }

            @Override
            public boolean isFeatureEnabledForCurrentUser(String featureKey)
            {
                return false;
            }

            @Override
            public boolean isFeatureEnabledForUser(@Nullable UserKey userKey, String featureKey)
            {
                return false;
            }

            @Override
            public boolean canManageFeaturesForAllUsers()
            {
                return false;
            }

            @Override
            public void enableFeatureForAllUsers(String featureKey)
            {

            }

            @Override
            public void disableFeatureForAllUsers(String featureKey)
            {

            }

            @Override
            public void enableFeatureForCurrentUser(String featureKey)
            {

            }

            @Override
            public void enableFeatureForUser(UserKey userKey, String featureKey)
            {

            }

            @Override
            public void disableFeatureForCurrentUser(String featureKey)
            {

            }

            @Override
            public void disableFeatureForUser(UserKey userKey, String featureKey)
            {

            }

            @Override
            public EnabledDarkFeatures getFeaturesEnabledForAllUsers()
            {
                return null;
            }

            @Override
            public EnabledDarkFeatures getFeaturesEnabledForCurrentUser()
            {
                return null;
            }

            @Override
            public EnabledDarkFeatures getFeaturesEnabledForUser(@Nullable UserKey userKey)
            {
                return null;
            }
        };
    }

    private static EventPublisher createEventPublisher()
    {
        return new EventPublisher()
        {
            @Override
            public void publish(Object o)
            {

            }

            @Override
            public void register(Object o)
            {

            }

            @Override
            public void unregister(Object o)
            {

            }

            @Override
            public void unregisterAll()
            {

            }
        };
    }
}
