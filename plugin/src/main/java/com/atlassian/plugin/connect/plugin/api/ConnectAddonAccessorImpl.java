package com.atlassian.plugin.connect.plugin.api;

import com.atlassian.plugin.connect.api.ConnectAddonAccessor;
import com.atlassian.plugin.connect.api.registry.ConnectAddonRegistry;
import com.atlassian.plugin.connect.plugin.installer.ConnectAddonManager;
import com.atlassian.plugin.spring.scanner.annotation.export.ExportAsService;

import javax.annotation.Nonnull;
import javax.inject.Inject;
import javax.inject.Named;

import static com.google.common.base.Preconditions.checkNotNull;

@Named
@ExportAsService
public class ConnectAddonAccessorImpl implements ConnectAddonAccessor
{
    private final ConnectAddonManager connectAddonManager;
    private final ConnectAddonRegistry connectAddonRegistry;

    @Inject
    public ConnectAddonAccessorImpl(ConnectAddonManager connectAddonManager, ConnectAddonRegistry connectAddonRegistry)
    {
        this.connectAddonManager = connectAddonManager;
        this.connectAddonRegistry = connectAddonRegistry;
    }

    @Override
    public String getDescriptor(@Nonnull final String addOnKey)
    {
        return connectAddonRegistry.getDescriptor(checkNotNull(addOnKey, "addOnKey"));
    }

    @Override
    public String getSharedSecret(@Nonnull String addOnKey)
    {
        return connectAddonRegistry.getSecret(checkNotNull(addOnKey, "addOnKey"));
    }

    @Override
    public boolean isAddonEnabled(@Nonnull final String addOnKey)
    {
        return connectAddonManager.isAddonEnabled(checkNotNull(addOnKey, "addOnKey"));
    }

}
