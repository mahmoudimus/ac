package com.atlassian.labs.remoteapps.host.common.service.confluence;

import com.atlassian.labs.remoteapps.api.service.confluence.ConfluenceSpaceClient;
import com.atlassian.labs.remoteapps.host.common.service.http.HostXmlRpcClientServiceFactory;
import com.atlassian.labs.remoteapps.spi.permission.PermissionsReader;
import com.atlassian.plugin.PluginAccessor;

/**
 */
public class ConfluenceSpaceClientServiceFactory extends AbstractConfluenceClientServiceFactory<ConfluenceSpaceClient>
{
    public ConfluenceSpaceClientServiceFactory(
            HostXmlRpcClientServiceFactory hostXmlRpcClientServiceFactory, PermissionsReader permissionsReader,
            PluginAccessor pluginAccessor)
    {
        super(ConfluenceSpaceClient.class, hostXmlRpcClientServiceFactory, permissionsReader, pluginAccessor);
    }
}
