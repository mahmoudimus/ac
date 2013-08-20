package com.atlassian.plugin.connect.plugin.module;

import java.net.URI;
import java.net.URISyntaxException;

abstract class AbstractIFrameHost implements IFrameHost
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
