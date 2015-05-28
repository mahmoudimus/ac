package com.atlassian.plugin.connect.core.api;

import com.atlassian.plugin.connect.api.ConnectAddonAccessor;
import com.atlassian.plugin.connect.core.installer.ConnectAddonManager;
import com.atlassian.plugin.spring.scanner.annotation.export.ExportAsService;

import javax.inject.Inject;
import javax.inject.Named;

@Named
@ExportAsService
public class ConnectAddonAccessorImpl implements ConnectAddonAccessor
{
    private final ConnectAddonManager connectAddonManager;

    @Inject
    public ConnectAddonAccessorImpl(ConnectAddonManager connectAddonManager)
    {
        this.connectAddonManager = connectAddonManager;
    }

    @Override
    public boolean isAddonEnabled(final String addOnKey)
    {
        return connectAddonManager.isAddonEnabled(addOnKey);
    }
}
