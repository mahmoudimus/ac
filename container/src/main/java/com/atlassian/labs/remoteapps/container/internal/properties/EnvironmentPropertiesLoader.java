package com.atlassian.labs.remoteapps.container.internal.properties;

import java.util.Map;

/**
 * A properties loader to load environment properties.
 */
public final class EnvironmentPropertiesLoader implements PropertiesLoader
{
    @Override
    public Map<String, String> load()
    {
        return System.getenv();
    }
}
