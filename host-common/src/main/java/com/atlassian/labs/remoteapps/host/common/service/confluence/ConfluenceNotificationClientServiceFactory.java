package com.atlassian.labs.remoteapps.host.common.service.confluence;

import com.atlassian.labs.remoteapps.api.service.confluence.ConfluenceNotificationClient;
import com.atlassian.labs.remoteapps.host.common.service.http.HostXmlRpcClientServiceFactory;
import com.atlassian.labs.remoteapps.spi.permission.PermissionsReader;
import com.atlassian.plugin.PluginAccessor;

/**
 */
public class ConfluenceNotificationClientServiceFactory extends AbstractConfluenceClientServiceFactory<ConfluenceNotificationClient>
{
    public ConfluenceNotificationClientServiceFactory(
            HostXmlRpcClientServiceFactory hostXmlRpcClientServiceFactory,
                        PermissionsReader permissionsReader,
                        PluginAccessor pluginAccessor)
    {
        super(ConfluenceNotificationClient.class, hostXmlRpcClientServiceFactory, permissionsReader, pluginAccessor);
    }
}
