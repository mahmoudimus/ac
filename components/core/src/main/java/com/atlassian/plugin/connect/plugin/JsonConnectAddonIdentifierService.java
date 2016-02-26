package com.atlassian.plugin.connect.plugin;

import javax.inject.Inject;
import javax.inject.Named;

@Named("jsonConnectAddonIdentifierService")
public class JsonConnectAddonIdentifierService implements ConnectAddonIdentifierService {
    private final ConnectAddonRegistry connectAddonRegistry;

    @Inject
    public JsonConnectAddonIdentifierService(ConnectAddonRegistry connectAddonRegistry) {
        this.connectAddonRegistry = connectAddonRegistry;
    }

    @Override
    public boolean isConnectAddon(final String pluginKey) {
        return connectAddonRegistry.hasDescriptor(pluginKey);
    }

}
