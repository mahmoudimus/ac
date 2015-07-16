package com.atlassian.plugin.connect.plugin.capabilities.event;

import com.atlassian.event.api.EventPublisher;
import com.atlassian.plugin.connect.api.registry.ConnectAddonRegistry;
import com.atlassian.plugin.connect.plugin.Jira7ComponentBridge;
import com.atlassian.plugin.connect.plugin.installer.ConnectAddonManager;
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
    private final Jira7ComponentBridge jira7ComponentBridge;

    @Inject
    public ConnectPluginEnabledHandler(ConnectAddonRegistry addonRegistry,
            ConnectAddonManager addonManager, EventPublisher eventPublisher, Jira7ComponentBridge jira7ComponentBridge)
    {
        super(eventPublisher);
        this.addonRegistry = addonRegistry;
        this.addonManager = addonManager;
        this.jira7ComponentBridge = jira7ComponentBridge;
    }

    @Override
    protected void finalInit()
    {
        jira7ComponentBridge.makeUserPropertyServiceAvailableInPluginContainer();
        enableAddons();
    }

    private void enableAddons()
    {
        //enable all the addons if needed
        for (String addonKey : addonRegistry.getAddonKeysToEnableOnRestart())
        {
            addonManager.enableConnectAddon(addonKey);
        }
    }
}
