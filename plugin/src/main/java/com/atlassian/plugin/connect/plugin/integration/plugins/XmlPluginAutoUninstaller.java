package com.atlassian.plugin.connect.plugin.integration.plugins;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.Collection;

import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.PluginAccessor;
import com.atlassian.plugin.PluginException;
import com.atlassian.plugin.connect.plugin.installer.ConnectAddonManager;
import com.atlassian.plugin.connect.plugin.service.LegacyAddOnIdentifierService;
import com.atlassian.plugin.connect.plugin.usermanagement.ConnectAddOnUserDisableException;
import com.atlassian.plugin.predicate.PluginPredicate;
import com.atlassian.plugin.spring.scanner.annotation.export.ExportAsService;
import com.atlassian.sal.api.lifecycle.LifecycleAware;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ExportAsService(LifecycleAware.class)
@Named
public class XmlPluginAutoUninstaller implements LifecycleAware
{
    private final PluginAccessor pluginAccessor;
    private final LegacyAddOnIdentifierService legacyAddOnIdentifierService;
    private final ConnectAddonManager connectAddonManager;
    private static final Logger log = LoggerFactory.getLogger(XmlPluginAutoUninstaller.class);

    @Inject
    public XmlPluginAutoUninstaller(PluginAccessor pluginAccessor,
                                    LegacyAddOnIdentifierService legacyAddOnIdentifierService,
                                    ConnectAddonManager connectAddonManager)
    {
        this.pluginAccessor = pluginAccessor;
        this.legacyAddOnIdentifierService = legacyAddOnIdentifierService;
        this.connectAddonManager = connectAddonManager;
    }

    @Override
    public void onStart()
    {
        final Collection<Plugin> legacyAddons = pluginAccessor.getPlugins(new PluginPredicate()
        {
            @Override
            public boolean matches(Plugin plugin)
            {
                return legacyAddOnIdentifierService.isConnectAddOn(plugin);
            }
        });

        for (Plugin legacyAddon : legacyAddons)
        {
            uninstall(legacyAddon);
        }

    }

    private void uninstall(Plugin plugin) throws PluginException
    {
        try
        {
            log.info("Automatically uninstalling legacy xml addon - " + plugin.getKey());

            connectAddonManager.uninstallConnectAddon(plugin.getKey());
        }
        catch (ConnectAddOnUserDisableException e)
        {
            log.error("Unable to uninstall connect addon fully - " + plugin.getKey(), e);
        }
    }

}
