package com.atlassian.plugin.connect.plugin.capabilities;

import com.atlassian.plugin.connect.api.registry.ConnectAddonRegistry;
import com.atlassian.plugin.connect.spi.ConnectAddOnIdentifierService;

import javax.inject.Inject;
import javax.inject.Named;

@Named("jsonConnectAddOnIdentifierService")
public class JsonConnectAddOnIdentifierService implements ConnectAddOnIdentifierService
{
    private final ConnectAddonRegistry connectAddonRegistry;

    @Inject
    public JsonConnectAddOnIdentifierService(ConnectAddonRegistry connectAddonRegistry)
    {
        this.connectAddonRegistry = connectAddonRegistry;
    }

    @Override
    public boolean isConnectAddOn(final String pluginKey)
    {
        return connectAddonRegistry.hasDescriptor(pluginKey);
    }

}
