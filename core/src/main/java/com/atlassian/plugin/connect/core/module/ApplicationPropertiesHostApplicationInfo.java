package com.atlassian.plugin.connect.core.module;

import com.atlassian.sal.api.ApplicationProperties;
import com.atlassian.sal.api.UrlMode;

import java.net.URI;

import static com.google.common.base.Preconditions.checkNotNull;

public final class ApplicationPropertiesHostApplicationInfo extends AbstractHostApplicationInfo
{
    private final ApplicationProperties applicationProperties;

    public ApplicationPropertiesHostApplicationInfo(ApplicationProperties applicationProperties)
    {
        this.applicationProperties = checkNotNull(applicationProperties);
    }

    @Override
    public String getContextPath()
    {
        return URI.create(applicationProperties.getBaseUrl(UrlMode.CANONICAL)).getPath();
    }

    @Override
    public URI getUrl()
    {
        return createEasyXdmHost(URI.create(applicationProperties.getBaseUrl(UrlMode.CANONICAL)));
    }
}
