package com.atlassian.plugin.connect.plugin.capabilities.event;

import com.atlassian.event.api.EventPublisher;
import com.atlassian.plugin.connect.api.util.ConnectPluginInfo;
import com.atlassian.plugin.connect.plugin.installer.ConnectAddonManager;
import com.atlassian.plugin.connect.api.registry.ConnectAddonRegistry;
import com.atlassian.plugin.connect.plugin.usermanagement.ConnectAddOnUserDisableException;
import com.atlassian.plugin.connect.plugin.util.AbstractInitializingComponent;
import com.atlassian.plugin.event.PluginEventListener;
import com.atlassian.plugin.event.events.BeforePluginDisabledEvent;
import com.atlassian.plugin.spring.scanner.annotation.export.ExportAsService;
import com.atlassian.sal.api.lifecycle.LifecycleAware;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;

import javax.inject.Inject;
import javax.inject.Named;
import java.io.IOException;

@Named
@ExportAsService(LifecycleAware.class)
public class ConnectPluginProperEventHandler extends AbstractInitializingComponent implements InitializingBean, DisposableBean
{
    private static final Logger log = LoggerFactory.getLogger(ConnectPluginProperEventHandler.class);
    private final ConnectAddonRegistry addonRegistry;
    private final ConnectAddonManager addonManager;

    @Inject
    public ConnectPluginProperEventHandler(ConnectAddonRegistry addonRegistry,
            ConnectAddonManager addonManager, EventPublisher eventPublisher)
    {
        super(eventPublisher, ConnectPluginInfo.getPluginKey());
        this.addonRegistry = addonRegistry;
        this.addonManager = addonManager;
    }

    @Override
    protected void finalInit()
    {
        //enable all the addons if needed
        for (String addonKey : addonRegistry.getAddonKeysToEnableOnRestart())
        {
            addonManager.enableConnectAddon(addonKey);
        }
    }

    @PluginEventListener
    @SuppressWarnings("unused")
    public void beforePluginDisabled(BeforePluginDisabledEvent beforePluginDisabledEvent) throws IOException
    {
        if (isTargetPlugin(beforePluginDisabledEvent.getPlugin()))
        {
            for (String pluginKey : addonRegistry.getAllAddonKeys())
            {
                try
                {
                    addonManager.disableConnectAddonWithoutPersistingState(pluginKey);
                }
                catch (ConnectAddOnUserDisableException e)
                {
                    log.error("Unable to disable addon user for addon: " + pluginKey, e);
                }
            }
        }
    }


}
