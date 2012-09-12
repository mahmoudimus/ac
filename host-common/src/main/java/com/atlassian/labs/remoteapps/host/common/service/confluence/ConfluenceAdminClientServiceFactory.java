package com.atlassian.labs.remoteapps.host.common.service.confluence;

import com.atlassian.labs.remoteapps.api.service.confluence.ConfluenceAdminClient;
import com.atlassian.labs.remoteapps.host.common.service.http.HostXmlRpcClientServiceFactory;
import com.atlassian.labs.remoteapps.spi.permission.PermissionsReader;
import com.atlassian.plugin.PluginAccessor;

/**
 */
public class ConfluenceAdminClientServiceFactory extends AbstractConfluenceClientServiceFactory<ConfluenceAdminClient>
{
    public ConfluenceAdminClientServiceFactory(
            HostXmlRpcClientServiceFactory hostXmlRpcClientServiceFactory,
            PermissionsReader permissionsReader,
            PluginAccessor pluginAccessor)
    {
        super(ConfluenceAdminClient.class, hostXmlRpcClientServiceFactory, permissionsReader, pluginAccessor);
    }
}
