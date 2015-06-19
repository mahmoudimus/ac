package com.atlassian.plugin.connect.plugin.api;

import com.atlassian.plugin.connect.api.ConnectAddonAccessor;
import com.atlassian.plugin.connect.plugin.installer.ConnectAddonManager;
import com.atlassian.plugin.connect.plugin.license.LicenseRetriever;
import com.atlassian.plugin.spring.scanner.annotation.export.ExportAsService;

import javax.inject.Inject;
import javax.inject.Named;

@Named
@ExportAsService
public class ConnectAddonAccessorImpl implements ConnectAddonAccessor
{
    private final ConnectAddonManager connectAddonManager;
    private final LicenseRetriever licenseRetriever;

    @Inject
    public ConnectAddonAccessorImpl(ConnectAddonManager connectAddonManager, final LicenseRetriever licenseRetriever)
    {
        this.connectAddonManager = connectAddonManager;
        this.licenseRetriever = licenseRetriever;
    }

    @Override
    public boolean isAddonEnabled(final String addonKey)
    {
        return connectAddonManager.isAddonEnabled(addonKey);
    }

}
