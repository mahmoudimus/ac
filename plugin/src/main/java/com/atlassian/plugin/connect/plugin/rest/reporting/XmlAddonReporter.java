package com.atlassian.plugin.connect.plugin.rest.reporting;

import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.PluginAccessor;
import com.atlassian.plugin.PluginState;
import com.atlassian.plugin.connect.plugin.service.LegacyAddOnIdentifierService;
import com.google.common.collect.Lists;

import java.util.List;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;

@Path ("report/xml")
public class XmlAddonReporter
{
    private final PluginAccessor pluginAccessor;
    private final LegacyAddOnIdentifierService legacyAddOnIdentifierService;

    public XmlAddonReporter(final PluginAccessor pluginAccessor, final LegacyAddOnIdentifierService legacyAddOnIdentifierService)
    {
        this.pluginAccessor = pluginAccessor;
        this.legacyAddOnIdentifierService = legacyAddOnIdentifierService;
    }

    @GET
    @Produces("application/json")
    public Response getXmlAddons()
    {
        List<XmlAddon> xmlAddons = Lists.newArrayList();

        for(Plugin plugin : pluginAccessor.getPlugins())
        {
            if(legacyAddOnIdentifierService.isConnectAddOn(plugin))
            {
                String key = plugin.getKey();
                String version = plugin.getPluginInformation().getVersion();
                String state = (PluginState.ENABLED.equals(plugin.getPluginState())) ? PluginState.ENABLED.name() : PluginState.DISABLED.name();

                xmlAddons.add(new XmlAddon(key, state, version));
            }
        }

        return Response.ok().entity(xmlAddons).build();
    }
}
