package com.atlassian.labs.remoteapps.host.common.service.confluence;

import com.atlassian.labs.remoteapps.api.service.confluence.ConfluenceUserClient;
import com.atlassian.labs.remoteapps.host.common.service.http.HostXmlRpcClientServiceFactory;
import com.atlassian.labs.remoteapps.spi.permission.PermissionsReader;
import com.atlassian.plugin.PluginAccessor;

/**
 */
public class ConfluenceUserClientServiceFactory extends AbstractConfluenceClientServiceFactory<ConfluenceUserClient>
{
    public ConfluenceUserClientServiceFactory(
            HostXmlRpcClientServiceFactory hostXmlRpcClientServiceFactory,
                        PermissionsReader permissionsReader,
                        PluginAccessor pluginAccessor)
    {
        super(ConfluenceUserClient.class, hostXmlRpcClientServiceFactory, permissionsReader, pluginAccessor);
    }
}
