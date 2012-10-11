package com.atlassian.plugin.remotable.host.common.service.confluence;

import com.atlassian.plugin.remotable.api.service.confluence.ConfluenceAttachmentClient;
import com.atlassian.plugin.remotable.host.common.service.RequestContextServiceFactory;
import com.atlassian.plugin.remotable.host.common.service.http.HostHttpClientServiceFactory;
import com.atlassian.plugin.remotable.host.common.service.http.HostXmlRpcClientServiceFactory;
import com.atlassian.plugin.remotable.spi.permission.PermissionsReader;
import com.atlassian.plugin.PluginAccessor;

/**
 */
public class ConfluenceAttachmentClientServiceFactory extends AbstractConfluenceClientServiceFactory<ConfluenceAttachmentClient>
{
    public ConfluenceAttachmentClientServiceFactory(
            HostHttpClientServiceFactory httpClient,
            HostXmlRpcClientServiceFactory hostXmlRpcClientServiceFactory,
                        PermissionsReader permissionsReader,
                        PluginAccessor pluginAccessor,
            RequestContextServiceFactory requestContextServiceFactory)
    {
        super(ConfluenceAttachmentClient.class, httpClient, hostXmlRpcClientServiceFactory, permissionsReader, pluginAccessor,
                requestContextServiceFactory);
    }
}
