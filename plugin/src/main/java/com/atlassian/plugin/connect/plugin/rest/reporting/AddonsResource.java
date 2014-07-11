package com.atlassian.plugin.connect.plugin.rest.reporting;

import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.PluginAccessor;
import com.atlassian.plugin.PluginController;
import com.atlassian.plugin.PluginException;
import com.atlassian.plugin.connect.modules.beans.ConnectAddonBean;
import com.atlassian.plugin.connect.plugin.license.LicenseRetriever;
import com.atlassian.plugin.connect.plugin.registry.ConnectAddonRegistry;
import com.atlassian.plugin.connect.plugin.service.LegacyAddOnIdentifierService;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;

/**
 * REST endpoint which provides a view of Connect add-ons which are installed in the instance.
 */
@Path("addons")
public class AddonsResource
{
    private static final Logger log = LoggerFactory.getLogger(AddonsResource.class);

    private final PluginAccessor pluginAccessor;
    private final PluginController pluginController;
    private final LegacyAddOnIdentifierService legacyAddOnIdentifierService;
    private final ConnectAddonRegistry addonRegistry;
    private final LicenseRetriever licenseRetriever;

    public AddonsResource(PluginAccessor pluginAccessor, PluginController pluginController,
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
        List<RestAddonStatus> restAddons = getAddons();
        return Response.ok().entity(restAddons).build();
    }

    @GET
    @Path("/xml")
    @Produces("application/json")
    public Response getXmlAddons()
    {
        List<RestAddonStatus> restAddons = getAddons(RestAddonType.XML);
        return Response.ok().entity(restAddons).build();
    }

    /**
     * Deletes all XML based Atlassian Connect add-ons from the instance
     */
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
                RestAddon addon = new RestAddon(key, version, RestAddonType.XML);
                pluginController.uninstall(plugin);
                addons.add(addon);
            }
            catch (PluginException e)
            {
                log.error("Unable to uninstall plugin " + plugin.getKey(), e);
                errors.add(e);
            }
        }

        result.put("addons", addons);
        result.put("errors", errors);

        return Response.ok().entity(result).build();
    }

    @GET
    @Path("/json")
    @Produces("application/json")
    public Response getJsonAddons()
    {
        List<RestAddonStatus> restAddons = getAddons(RestAddonType.JSON);
        return Response.ok().entity(restAddons).build();
    }

    private List<RestAddonStatus> getAddons()
    {
        return getAddons(null);
    }

    private List<RestAddonStatus> getAddons(RestAddonType type)
    {
        List<RestAddonStatus> result = Lists.newArrayList();

        if (type == null || type == RestAddonType.XML)
        {
            for (Plugin plugin : getXmlAddonPlugins())
            {
                String key = plugin.getKey();
                String version = plugin.getPluginInformation().getVersion();
                String state = plugin.getPluginState().name();
                String license = licenseRetriever.getLicenseStatus(key).value();

                result.add(new RestAddonStatus(key, version, RestAddonType.XML, state, license));
            }
        }

        if (type == null || type == RestAddonType.JSON)
        {
            for (ConnectAddonBean addonBean : addonRegistry.getAllAddonBeans())
            {
                String key = addonBean.getKey();
                String version = addonBean.getVersion();
                String state = addonRegistry.getRestartState(key).name();
                String license = licenseRetriever.getLicenseStatus(key).value();

                result.add(new RestAddonStatus(key, version, RestAddonType.JSON, state, license));
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
