package com.atlassian.plugin.connect.plugin.capabilities.event;

import java.io.IOException;

import javax.inject.Inject;
import javax.inject.Named;

import com.atlassian.event.api.EventPublisher;
import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.connect.api.registry.ConnectAddonRegistry;
import com.atlassian.plugin.connect.api.util.ConnectPluginInfo;
import com.atlassian.plugin.connect.plugin.installer.ConnectAddonManager;
import com.atlassian.plugin.connect.plugin.usermanagement.ConnectAddOnUserDisableException;
import com.atlassian.plugin.event.PluginEventListener;
import com.atlassian.plugin.event.events.BeforePluginDisabledEvent;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;

@Named
public class ConnectPluginDisabledHandler implements InitializingBean, DisposableBean
{
    private static final Logger log = LoggerFactory.getLogger(ConnectPluginDisabledHandler.class);
    private final EventPublisher eventPublisher;
    private final ConnectAddonRegistry addonRegistry;
    private final ConnectAddonManager addonManager;

    @Inject
    public ConnectPluginDisabledHandler(final ConnectAddonRegistry addonRegistry,
            final ConnectAddonManager addonManager, final EventPublisher eventPublisher)
    {
        this.eventPublisher = eventPublisher;
        this.addonRegistry = addonRegistry;
        this.addonManager = addonManager;
    }

    @PluginEventListener
    @SuppressWarnings("unused")
    public void beforePluginDisabled(BeforePluginDisabledEvent beforePluginDisabledEvent) throws IOException
    {
        if (isTheConnectPlugin(beforePluginDisabledEvent.getPlugin()))
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

    private boolean isTheConnectPlugin(Plugin plugin)
    {
        return (ConnectPluginInfo.getPluginKey().equals(plugin.getKey()));
    }

    @Override
    public void afterPropertiesSet() throws Exception
    {
        eventPublisher.register(this);
    }

    @Override
    public void destroy() throws Exception
    {
        eventPublisher.unregister(this);
    }
}
