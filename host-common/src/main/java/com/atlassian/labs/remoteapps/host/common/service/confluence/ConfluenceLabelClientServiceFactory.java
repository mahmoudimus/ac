package com.atlassian.labs.remoteapps.host.common.service.confluence;

import com.atlassian.labs.remoteapps.api.service.confluence.ConfluenceLabelClient;
import com.atlassian.labs.remoteapps.host.common.service.http.HostXmlRpcClientServiceFactory;
import com.atlassian.labs.remoteapps.spi.permission.PermissionsReader;
import com.atlassian.plugin.PluginAccessor;

/**
 */
public class ConfluenceLabelClientServiceFactory extends AbstractConfluenceClientServiceFactory<ConfluenceLabelClient>
{
    public ConfluenceLabelClientServiceFactory(
            HostXmlRpcClientServiceFactory hostXmlRpcClientServiceFactory,
                        PermissionsReader permissionsReader,
                        PluginAccessor pluginAccessor)
    {
        super(ConfluenceLabelClient.class, hostXmlRpcClientServiceFactory, permissionsReader, pluginAccessor);
    }
}
