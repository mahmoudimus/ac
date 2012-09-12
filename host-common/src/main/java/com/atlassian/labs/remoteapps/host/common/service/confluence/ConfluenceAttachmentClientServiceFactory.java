package com.atlassian.labs.remoteapps.host.common.service.confluence;

import com.atlassian.labs.remoteapps.api.service.confluence.ConfluenceAttachmentClient;
import com.atlassian.labs.remoteapps.host.common.service.http.HostXmlRpcClientServiceFactory;
import com.atlassian.labs.remoteapps.spi.permission.PermissionsReader;
import com.atlassian.plugin.PluginAccessor;

/**
 */
public class ConfluenceAttachmentClientServiceFactory extends AbstractConfluenceClientServiceFactory<ConfluenceAttachmentClient>
{
    public ConfluenceAttachmentClientServiceFactory(
            HostXmlRpcClientServiceFactory hostXmlRpcClientServiceFactory,
                        PermissionsReader permissionsReader,
                        PluginAccessor pluginAccessor)
    {
        super(ConfluenceAttachmentClient.class, hostXmlRpcClientServiceFactory, permissionsReader, pluginAccessor);
    }
}
