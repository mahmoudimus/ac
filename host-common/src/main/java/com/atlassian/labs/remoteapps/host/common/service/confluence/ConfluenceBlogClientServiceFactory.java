package com.atlassian.labs.remoteapps.host.common.service.confluence;

import com.atlassian.labs.remoteapps.api.service.confluence.ConfluenceBlogClient;
import com.atlassian.labs.remoteapps.host.common.service.http.HostXmlRpcClientServiceFactory;
import com.atlassian.labs.remoteapps.spi.permission.PermissionsReader;
import com.atlassian.plugin.PluginAccessor;

/**
 */
public class ConfluenceBlogClientServiceFactory extends AbstractConfluenceClientServiceFactory<ConfluenceBlogClient>
{
    public ConfluenceBlogClientServiceFactory(
            HostXmlRpcClientServiceFactory hostXmlRpcClientServiceFactory,
                        PermissionsReader permissionsReader,
                        PluginAccessor pluginAccessor)
    {
        super(ConfluenceBlogClient.class, hostXmlRpcClientServiceFactory, permissionsReader, pluginAccessor);
    }
}
