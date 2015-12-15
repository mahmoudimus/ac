package com.atlassian.plugin.connect.plugin;


import com.atlassian.plugin.connect.api.ConnectAddonController;
import com.atlassian.plugin.connect.api.lifecycle.ConnectAddonEnableException;
import com.atlassian.plugin.connect.api.lifecycle.ConnectAddonInstallException;
import com.atlassian.plugin.connect.plugin.lifecycle.ConnectAddOnInstaller;
import com.atlassian.plugin.connect.plugin.lifecycle.ConnectAddonManager;
import com.atlassian.plugin.connect.api.lifecycle.ConnectAddonDisableException;
import com.atlassian.plugin.spring.scanner.annotation.export.ExportAsService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.inject.Inject;

@Component
@ExportAsService
public class ConnectAddonControllerImpl implements ConnectAddonController
{
    private static final Logger log = LoggerFactory.getLogger(ConnectAddonControllerImpl.class);

    private final ConnectAddonManager addonManager;
    private final ConnectAddOnInstaller addonInstaller;

    @Inject
    public ConnectAddonControllerImpl(ConnectAddonManager addonManager,
                                      ConnectAddOnInstaller addonInstaller)
    {
        this.addonManager = addonManager;
        this.addonInstaller = addonInstaller;
    }

    @Override
    public void enableAddon(String addonKey) throws ConnectAddonEnableException
    {
        addonManager.enableConnectAddon(addonKey);
    }

    @Override
    public void disableAddon(String addonKey)
    {
        try
        {
            addonManager.disableConnectAddon(addonKey);
        }
        catch (ConnectAddonDisableException e)
        {
            log.error("Unable to disable addon user for addon: " + addonKey, e);
        }
    }

    @Override
    public void installAddon(String jsonDescriptor) throws ConnectAddonInstallException
    {
        addonInstaller.install(jsonDescriptor);
    }

    @Override
    public void uninstallAddon(String addonKey)
    {
        try
        {
            addonManager.uninstallConnectAddon(addonKey);
        }
        catch (ConnectAddonDisableException e)
        {
            log.error("Unable to disable addon user for addon: " + addonKey, e);
        }
    }
}
