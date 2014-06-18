package com.atlassian.plugin.connect.plugin.xmldescriptor;

import com.atlassian.event.api.EventPublisher;
import com.atlassian.sal.api.features.DarkFeatureManager;
import com.atlassian.sal.api.features.EnabledDarkFeatures;
import com.atlassian.sal.api.user.UserKey;
import org.apache.commons.lang.NotImplementedException;

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
                return Boolean.getBoolean(featureKey); // allow dark features to be enabled in unit tests via "-Dsome.property=true" command line parameters
            }

            @Override
            public boolean isFeatureEnabledForCurrentUser(String featureKey)
            {
                return isFeatureEnabledForAllUsers(featureKey);
            }

            @Override
            public boolean isFeatureEnabledForUser(@Nullable UserKey userKey, String featureKey)
            {
                return isFeatureEnabledForAllUsers(featureKey);
            }

            @Override
            public boolean canManageFeaturesForAllUsers()
            {
                return false;
            }

            @Override
            public void enableFeatureForAllUsers(String featureKey)
            {
                throw new NotImplementedException();
            }

            @Override
            public void disableFeatureForAllUsers(String featureKey)
            {
                throw new NotImplementedException();
            }

            @Override
            public void enableFeatureForCurrentUser(String featureKey)
            {
                throw new NotImplementedException();
            }

            @Override
            public void enableFeatureForUser(UserKey userKey, String featureKey)
            {
                throw new NotImplementedException();
            }

            @Override
            public void disableFeatureForCurrentUser(String featureKey)
            {
                throw new NotImplementedException();
            }

            @Override
            public void disableFeatureForUser(UserKey userKey, String featureKey)
            {
                throw new NotImplementedException();
            }

            @Override
            public EnabledDarkFeatures getFeaturesEnabledForAllUsers()
            {
                throw new NotImplementedException();
            }

            @Override
            public EnabledDarkFeatures getFeaturesEnabledForCurrentUser()
            {
                throw new NotImplementedException();
            }

            @Override
            public EnabledDarkFeatures getFeaturesEnabledForUser(@Nullable UserKey userKey)
            {
                throw new NotImplementedException();
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
