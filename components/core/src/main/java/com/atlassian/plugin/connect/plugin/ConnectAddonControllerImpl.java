package com.atlassian.plugin.connect.plugin;


import com.atlassian.plugin.connect.api.ConnectAddonController;
import com.atlassian.plugin.connect.plugin.lifecycle.ConnectAddOnInstaller;
import com.atlassian.plugin.connect.plugin.lifecycle.ConnectAddonManager;
import com.atlassian.plugin.connect.spi.auth.user.ConnectAddOnUserDisableException;
import com.atlassian.plugin.spring.scanner.annotation.export.ExportAsService;
import org.springframework.stereotype.Component;

import javax.inject.Inject;

@Component
@ExportAsService
public class ConnectAddonControllerImpl implements ConnectAddonController
{
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
    public void enableAddons(String... addonKeys)
    {
        for (String addonKey : addonKeys)
        {
            addonManager.enableConnectAddon(addonKey);
        }
    }
    
    @Override
    public void disableAddon(String addonKey) throws ConnectAddOnUserDisableException
    {
        addonManager.disableConnectAddon(addonKey);
    }
    
    @Override
    public void disableAddonWithoutPersistingState(String addonKey) throws ConnectAddOnUserDisableException
    {
        addonManager.disableConnectAddonWithoutPersistingState(addonKey);
        
    }
    
    @Override
    public void installAddons(String... jsonDescriptors)
    {
        for (String jsonDescriptor : jsonDescriptors)
        {
            addonInstaller.install(jsonDescriptor);
        }
    }
    
    @Override
    public void uninstallAddon(String addonKey) throws ConnectAddOnUserDisableException
    {
        addonManager.uninstallConnectAddon(addonKey);
    }
}
