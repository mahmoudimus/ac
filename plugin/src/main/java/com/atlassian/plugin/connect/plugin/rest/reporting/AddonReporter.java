package com.atlassian.plugin.connect.plugin.rest.reporting;

import com.atlassian.plugin.*;
import com.atlassian.plugin.connect.modules.beans.ConnectAddonBean;
import com.atlassian.plugin.connect.plugin.license.LicenseRetriever;
import com.atlassian.plugin.connect.plugin.registry.ConnectAddonRegistry;
import com.atlassian.plugin.connect.plugin.service.LegacyAddOnIdentifierService;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;

/**
 * REST endpoint which returns a list of Connect add-ons which are installed in the current instance.
 */
@Path("report")
public class AddonReporter
{
    private static final Logger log = LoggerFactory.getLogger(AddonReporter.class);

    private static final int TYPE_XML = 1;
    private static final int TYPE_JSON = 2;

    private final PluginAccessor pluginAccessor;
    private final PluginController pluginController;
    private final LegacyAddOnIdentifierService legacyAddOnIdentifierService;
    private final ConnectAddonRegistry addonRegistry;
    private final LicenseRetriever licenseRetriever;

    public AddonReporter(PluginAccessor pluginAccessor, PluginController pluginController,
                         LegacyAddOnIdentifierService legacyAddOnIdentifierService,
                         ConnectAddonRegistry addonRegistry, LicenseRetriever licenseRetriever)
    {
        this.pluginAccessor = pluginAccessor;
        this.pluginController = pluginController;
        this.legacyAddOnIdentifierService = legacyAddOnIdentifierService;
        this.addonRegistry = addonRegistry;
        this.licenseRetriever = licenseRetriever;
    }

    @GET
    @Produces("application/json")
    public Response getAllAddons()
    {
        List<RestAddonStatus> restAddons = getAddons(TYPE_XML | TYPE_JSON);
        return Response.ok().entity(restAddons).build();
    }

    @GET
    @Path("/xml")
    @Produces("application/json")
    public Response getXmlAddons()
    {
        List<RestAddonStatus> restAddons = getAddons(TYPE_XML);
        return Response.ok().entity(restAddons).build();
    }

    @SuppressWarnings("unchecked")
    @DELETE
    @Path("/xml")
    @Produces("application/json")
    public Response deleteXmlAddons()
    {
        List<RestAddon> addons = Lists.newArrayList();
        List<PluginException> errors = Lists.newArrayList();

        Map result = Maps.newHashMap();

        List<Plugin> xmlPlugins = getXmlAddonPlugins();

        for (Plugin plugin : xmlPlugins)
        {
            try
            {
                String key = plugin.getKey();
                String version = plugin.getPluginInformation().getVersion();
                RestAddon addon = new RestAddon(key, version);
                pluginController.uninstall(plugin);
                addons.add(addon);
            }
            catch (PluginException e)
            {
                log.error("Unable to uninstall plugin " + plugin.getKey(), e);
                errors.add(e);
            }
        }

        if (!addons.isEmpty())
        {
            result.put("addons", addons);
        }

        if (!errors.isEmpty())
        {
            result.put("errors", errors);
        }

        return Response.ok().entity(result).build();
    }

    @GET
    @Path("/json")
    @Produces("application/json")
    public Response getJsonAddons()
    {
        List<RestAddonStatus> restAddons = getAddons(TYPE_JSON);
        return Response.ok().entity(restAddons).build();
    }

    private List<RestAddonStatus> getAddons(int type)
    {
        List<RestAddonStatus> result = Lists.newArrayList();

        if ((type & TYPE_XML) == TYPE_XML)
        {
            for (Plugin plugin : getXmlAddonPlugins())
            {
                String key = plugin.getKey();
                String version = plugin.getPluginInformation().getVersion();
                String state = plugin.getPluginState().name();
                String license = licenseRetriever.getLicenseStatus(key).value();

                result.add(new RestAddonStatus(key, state, version, license));
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

                result.add(new RestAddonStatus(key, state, version, license));
            }
        }

        return result;
    }

    private List<Plugin> getXmlAddonPlugins()
    {
        List<Plugin> xmlPlugins = Lists.newArrayList();
        for (Plugin plugin : pluginAccessor.getPlugins())
        {
            if (legacyAddOnIdentifierService.isConnectAddOn(plugin))
            {
                xmlPlugins.add(plugin);
            }
        }
        return xmlPlugins;
    }
}
