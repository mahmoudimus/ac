package com.atlassian.labs.remoteapps.host.common.service.confluence;

import com.atlassian.labs.remoteapps.api.service.confluence.ConfluencePageClient;
import com.atlassian.labs.remoteapps.host.common.service.http.HostXmlRpcClientServiceFactory;
import com.atlassian.labs.remoteapps.spi.permission.PermissionsReader;
import com.atlassian.plugin.PluginAccessor;

/**
 */
public class ConfluencePageClientServiceFactory extends AbstractConfluenceClientServiceFactory<ConfluencePageClient>
{
    public ConfluencePageClientServiceFactory(HostXmlRpcClientServiceFactory hostXmlRpcClientServiceFactory,
                PermissionsReader permissionsReader,
                PluginAccessor pluginAccessor)
    {
        super(ConfluencePageClient.class, hostXmlRpcClientServiceFactory, permissionsReader, pluginAccessor);
    }
}
