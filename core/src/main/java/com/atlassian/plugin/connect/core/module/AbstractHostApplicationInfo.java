package com.atlassian.plugin.connect.core.module;

import java.net.URI;
import java.net.URISyntaxException;

abstract class AbstractHostApplicationInfo implements HostApplicationInfo
{
    protected URI createEasyXdmHost(URI baseUri)
    {
        try
        {
            return new URI(baseUri.getScheme(), null, baseUri.getHost(), baseUri.getPort(), null, null, null);
        }
        catch (URISyntaxException e)
        {
            throw new RuntimeException(e);
        }
    }
}
