package com.atlassian.plugin.connect.plugin.integration.plugins;

import com.atlassian.applinks.api.ApplicationLink;
import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.PluginAccessor;
import com.atlassian.plugin.PluginController;
import com.atlassian.plugin.PluginException;
import com.atlassian.plugin.connect.plugin.OAuthLinkManager;
import com.atlassian.plugin.connect.plugin.applinks.ConnectApplinkManager;
import com.atlassian.plugin.connect.plugin.service.LegacyAddOnIdentifierService;
import com.atlassian.plugin.predicate.PluginPredicate;
import com.atlassian.plugin.spring.scanner.annotation.export.ExportAsDevService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import java.util.Collection;

@ExportAsDevService
@Component
public class XmlPluginAutoUninstallHelperImpl implements XmlPluginAutoUninstallHelper
{
    private static final Logger log = LoggerFactory.getLogger(XmlPluginAutoUninstallHelperImpl.class);

    private final PluginAccessor pluginAccessor;
    private final LegacyAddOnIdentifierService legacyAddOnIdentifierService;
    private final PluginController pluginController;
    private final OAuthLinkManager oAuthLinkManager;
    private final ConnectApplinkManager connectApplinkManager;

    @Inject
    public XmlPluginAutoUninstallHelperImpl(PluginAccessor pluginAccessor,
                                            LegacyAddOnIdentifierService legacyAddOnIdentifierService,
                                            PluginController pluginController,
                                            OAuthLinkManager oAuthLinkManager,
                                            ConnectApplinkManager connectApplinkManager)
    {
        this.pluginController = pluginController;
        this.oAuthLinkManager = oAuthLinkManager;
        this.connectApplinkManager = connectApplinkManager;
        this.pluginAccessor = pluginAccessor;
        this.legacyAddOnIdentifierService = legacyAddOnIdentifierService;
    }

    @Override
    public void uninstallXmlPlugins()
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

    private void uninstall(Plugin plugin)
    {
        final String pluginKey = plugin.getKey();
        try
        {
            log.info("Automatically uninstalling legacy xml addon - " + pluginKey);

            pluginController.uninstall(plugin);

            final ApplicationLink appLink = connectApplinkManager.getAppLink(pluginKey);
            if (appLink != null)
            {
                // Blow away the applink
                oAuthLinkManager.unassociateProviderWithLink(appLink);
                connectApplinkManager.deleteAppLink(pluginKey);
            }
        }
        catch (Exception e)
        {
            log.error("Unable to uninstall connect addon fully - " + pluginKey, e);
        }
    }

}
