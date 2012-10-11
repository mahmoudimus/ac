package com.atlassian.plugin.remotable.host.common.service.confluence;

import com.atlassian.plugin.remotable.api.service.confluence.ConfluenceUserClient;
import com.atlassian.plugin.remotable.host.common.service.RequestContextServiceFactory;
import com.atlassian.plugin.remotable.host.common.service.http.HostHttpClientServiceFactory;
import com.atlassian.plugin.remotable.host.common.service.http.HostXmlRpcClientServiceFactory;
import com.atlassian.plugin.remotable.spi.permission.PermissionsReader;
import com.atlassian.plugin.PluginAccessor;

/**
 */
public class ConfluenceUserClientServiceFactory extends AbstractConfluenceClientServiceFactory<ConfluenceUserClient>
{
    public ConfluenceUserClientServiceFactory(
            HostHttpClientServiceFactory httpClient,
            HostXmlRpcClientServiceFactory hostXmlRpcClientServiceFactory,
                        PermissionsReader permissionsReader,
                        PluginAccessor pluginAccessor,
            RequestContextServiceFactory requestContextServiceFactory)
    {
        super(ConfluenceUserClient.class, httpClient, hostXmlRpcClientServiceFactory, permissionsReader, pluginAccessor,
                requestContextServiceFactory);
    }
}
