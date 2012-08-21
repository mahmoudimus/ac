package com.atlassian.labs.remoteapps.plugin.module;

import com.atlassian.sal.api.ApplicationProperties;

import java.net.URI;

import static com.google.common.base.Preconditions.*;

public final class ApplicationPropertiesIFrameHost extends AbstractIFrameHost
{
    private final ApplicationProperties applicationProperties;

    public ApplicationPropertiesIFrameHost(ApplicationProperties applicationProperties)
    {
        this.applicationProperties = checkNotNull(applicationProperties);
    }

    @Override
    public String getContextPath()
    {
        return URI.create(applicationProperties.getBaseUrl()).getPath();
    }

    @Override
    String extractUrl()
    {
        return applicationProperties.getBaseUrl();
    }
}
