package com.atlassian.plugin.remotable.host.common.service.confluence;

import com.atlassian.plugin.remotable.api.service.confluence.ConfluenceNotificationClient;
import com.atlassian.plugin.remotable.host.common.service.RequestContextServiceFactory;
import com.atlassian.plugin.remotable.host.common.service.http.HostHttpClientServiceFactory;
import com.atlassian.plugin.remotable.host.common.service.http.HostXmlRpcClientServiceFactory;
import com.atlassian.plugin.remotable.spi.permission.PermissionsReader;
import com.atlassian.plugin.PluginAccessor;

/**
 */
public class ConfluenceNotificationClientServiceFactory extends AbstractConfluenceClientServiceFactory<ConfluenceNotificationClient>
{
    public ConfluenceNotificationClientServiceFactory(
            HostHttpClientServiceFactory httpClient,
            HostXmlRpcClientServiceFactory hostXmlRpcClientServiceFactory,
                        PermissionsReader permissionsReader,
                        PluginAccessor pluginAccessor,
            RequestContextServiceFactory requestContextServiceFactory)
    {
        super(ConfluenceNotificationClient.class, httpClient, hostXmlRpcClientServiceFactory, permissionsReader, pluginAccessor,
                requestContextServiceFactory);
    }
}
