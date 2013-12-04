package com.atlassian.plugin.connect.plugin.module;

import java.net.URI;

import com.atlassian.sal.api.ApplicationProperties;
import com.atlassian.sal.api.UrlMode;

import static com.google.common.base.Preconditions.checkNotNull;

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
        return URI.create(applicationProperties.getBaseUrl(UrlMode.CANONICAL)).getPath();
    }

    @Override
    public URI getUrl()
    {
        return createEasyXdmHost(URI.create(applicationProperties.getBaseUrl(UrlMode.CANONICAL)));
    }
}
