package com.atlassian.plugin.connect.plugin.rest.reporting;

import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.PluginAccessor;
import com.atlassian.plugin.PluginState;
import com.atlassian.plugin.connect.modules.beans.ConnectAddonBean;
import com.atlassian.plugin.connect.plugin.license.LicenseRetriever;
import com.atlassian.plugin.connect.plugin.registry.ConnectAddonRegistry;
import com.atlassian.plugin.connect.plugin.service.LegacyAddOnIdentifierService;
import com.google.common.collect.Lists;

import java.util.List;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;

@Path ("report")
public class AddonReporter
{
    private static final int TYPE_XML = 1;
    private static final int TYPE_JSON = 2;

    private final PluginAccessor pluginAccessor;
    private final LegacyAddOnIdentifierService legacyAddOnIdentifierService;
    private final ConnectAddonRegistry addonRegistry;
    private final LicenseRetriever licenseRetriever;

    public AddonReporter(final PluginAccessor pluginAccessor, final LegacyAddOnIdentifierService legacyAddOnIdentifierService,
                         ConnectAddonRegistry addonRegistry, LicenseRetriever licenseRetriever)
    {
        this.pluginAccessor = pluginAccessor;
        this.legacyAddOnIdentifierService = legacyAddOnIdentifierService;
        this.addonRegistry = addonRegistry;
        this.licenseRetriever = licenseRetriever;
    }

    @GET
    @Produces("application/json")
    public Response getAllAddons()
    {
        List<RestAddon> restAddons = getAddons(TYPE_XML | TYPE_JSON);
        return Response.ok().entity(restAddons).build();
    }

    @GET
    @Path("/xml")
    @Produces("application/json")
    public Response getXmlAddons()
    {
        List<RestAddon> restAddons = getAddons(TYPE_XML);
        return Response.ok().entity(restAddons).build();
    }

    @GET
    @Path("/json")
    @Produces("application/json")
    public Response getJsonAddons()
    {
        List<RestAddon> restAddons = getAddons(TYPE_JSON);
        return Response.ok().entity(restAddons).build();
    }

    private List<RestAddon> getAddons(int type)
    {
        List<RestAddon> result = Lists.newArrayList();

        if ((type & TYPE_XML) == TYPE_XML) {
            for(Plugin plugin : pluginAccessor.getPlugins())
            {
                if(legacyAddOnIdentifierService.isConnectAddOn(plugin))
                {
                    String key = plugin.getKey();
                    String version = plugin.getPluginInformation().getVersion();
                    String state = plugin.getPluginState().name();
                    String license = licenseRetriever.getLicenseStatus(key).value();

                    result.add(new RestAddon(key, state, version, license));
                }
            }
        }

        if ((type & TYPE_JSON) == TYPE_JSON)
        {
            for (ConnectAddonBean addonBean : addonRegistry.getAllAddonBeans())
            {
                String key = addonBean.getKey();
                String version = addonBean.getVersion();
                String state = addonRegistry.getRestartState(key).name();
                String license = licenseRetriever.getLicenseStatus(key).value();

                result.add(new RestAddon(key, state, version, license));
            }
        }

        return result;
    }
}
