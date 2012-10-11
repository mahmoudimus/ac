package com.atlassian.plugin.remotable.host.common.service.confluence;

import com.atlassian.plugin.remotable.api.service.confluence.ConfluencePageClient;
import com.atlassian.plugin.remotable.host.common.service.RequestContextServiceFactory;
import com.atlassian.plugin.remotable.host.common.service.http.HostHttpClientServiceFactory;
import com.atlassian.plugin.remotable.host.common.service.http.HostXmlRpcClientServiceFactory;
import com.atlassian.plugin.remotable.spi.permission.PermissionsReader;
import com.atlassian.plugin.PluginAccessor;

/**
 */
public class ConfluencePageClientServiceFactory extends AbstractConfluenceClientServiceFactory<ConfluencePageClient>
{
    public ConfluencePageClientServiceFactory(
            HostHttpClientServiceFactory httpClient,HostXmlRpcClientServiceFactory hostXmlRpcClientServiceFactory,
                PermissionsReader permissionsReader,
                PluginAccessor pluginAccessor,
            RequestContextServiceFactory requestContextServiceFactory)
    {
        super(ConfluencePageClient.class, httpClient, hostXmlRpcClientServiceFactory, permissionsReader, pluginAccessor,
                requestContextServiceFactory);
    }
}
