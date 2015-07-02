package com.atlassian.plugin.connect.plugin.capabilities.event;

import com.atlassian.event.api.EventPublisher;
import com.atlassian.plugin.connect.api.util.ConnectPluginInfo;
import com.atlassian.plugin.connect.plugin.installer.ConnectAddonManager;
import com.atlassian.plugin.connect.api.registry.ConnectAddonRegistry;
import com.atlassian.plugin.connect.plugin.util.AbstractInitializingComponent;
import com.atlassian.plugin.spring.scanner.annotation.export.ExportAsService;
import com.atlassian.sal.api.lifecycle.LifecycleAware;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Named;

@Named
@ExportAsService(LifecycleAware.class)
public class ConnectPluginEnabledHandler extends AbstractInitializingComponent
{
    private static final Logger log = LoggerFactory.getLogger(ConnectPluginEnabledHandler.class);
    private final ConnectAddonRegistry addonRegistry;
    private final ConnectAddonManager addonManager;

    @Inject
    public ConnectPluginEnabledHandler(ConnectAddonRegistry addonRegistry,
            ConnectAddonManager addonManager, EventPublisher eventPublisher)
    {
        super(eventPublisher, ConnectPluginInfo.getPluginKey());
        this.addonRegistry = addonRegistry;
        this.addonManager = addonManager;
    }

    @Override
    protected void finalInit()
    {
        pluginEnabled();
    }

    private void pluginEnabled()
    {
        //enable all the addons if needed
        for (String addonKey : addonRegistry.getAddonKeysToEnableOnRestart())
        {
            addonManager.enableConnectAddon(addonKey);
        }
    }

}
