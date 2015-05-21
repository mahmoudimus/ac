package com.atlassian.plugin.connect.plugin.service;

import com.atlassian.plugin.connect.api.service.AddOnService;
import com.atlassian.plugin.connect.plugin.installer.ConnectAddonManager;
import com.atlassian.plugin.spring.scanner.annotation.export.ExportAsService;

import javax.inject.Inject;
import javax.inject.Named;

@Named
@ExportAsService
public class AddOnServiceImpl implements AddOnService
{
    private final ConnectAddonManager connectAddonManager;

    @Inject
    public AddOnServiceImpl(ConnectAddonManager connectAddonManager)
    {
        this.connectAddonManager = connectAddonManager;
    }

    @Override
    public boolean isAddOnEnabled(final String connectAddOnKey)
    {
        return connectAddonManager.isAddonEnabled(connectAddOnKey);
    }
}
