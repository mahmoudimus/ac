package com.atlassian.plugin.connect.plugin.lifecycle;

import com.atlassian.event.api.EventPublisher;
import com.atlassian.plugin.connect.plugin.ConnectAddonRegistry;
import com.atlassian.plugin.connect.plugin.util.AbstractInitializingComponent;
import com.atlassian.plugin.spring.scanner.annotation.export.ExportAsService;
import com.atlassian.sal.api.lifecycle.LifecycleAware;

import javax.inject.Inject;
import javax.inject.Named;

@Named
@ExportAsService(LifecycleAware.class)
public class ConnectPluginEnabledHandler extends AbstractInitializingComponent
{
    private final ConnectAddonRegistry addonRegistry;
    private final ConnectAddonManager addonManager;

    @Inject
    public ConnectPluginEnabledHandler(ConnectAddonRegistry addonRegistry,
            ConnectAddonManager addonManager, EventPublisher eventPublisher)
    {
        super(eventPublisher);
        this.addonRegistry = addonRegistry;
        this.addonManager = addonManager;
    }

    @Override
    protected void finalInit()
    {
        enableAddons();
    }

    private void enableAddons()
    {
        //enable all the addons if needed
        for (String addonKey : addonRegistry.getAddonKeysToEnableOnRestart())
        {
            addonManager.enableConnectAddonAndCatchFailure(addonKey);
        }
    }
}
