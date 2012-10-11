package com.atlassian.plugin.remotable.container.service.sal;

import com.atlassian.sal.api.ApplicationProperties;

import java.io.File;
import java.util.Date;

/**
 * Simple implementation of application properties
 */
public class ContainerApplicationProperties implements ApplicationProperties
{
    private final String baseUrl;

    public ContainerApplicationProperties(String baseUrl)
    {
        this.baseUrl = baseUrl;
    }

    @Override
    public String getBaseUrl()
    {
        return baseUrl;
    }

    @Override
    public String getDisplayName()
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public String getVersion()
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public Date getBuildDate()
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public String getBuildNumber()
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public File getHomeDirectory()
    {
        return new File(".");
    }

    @Override
    public String getPropertyValue(String key)
    {
        throw new UnsupportedOperationException();
    }
}
