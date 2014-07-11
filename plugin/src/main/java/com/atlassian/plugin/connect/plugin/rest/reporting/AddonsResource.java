package com.atlassian.plugin.connect.plugin.rest.reporting;

import com.atlassian.applinks.api.ApplicationLink;
import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.PluginAccessor;
import com.atlassian.plugin.PluginController;
import com.atlassian.plugin.PluginException;
import com.atlassian.plugin.connect.modules.beans.ConnectAddonBean;
import com.atlassian.plugin.connect.plugin.applinks.ConnectApplinkManager;
import com.atlassian.plugin.connect.plugin.license.LicenseRetriever;
import com.atlassian.plugin.connect.plugin.registry.ConnectAddonRegistry;
import com.atlassian.plugin.connect.plugin.rest.RestError;
import com.atlassian.plugin.connect.plugin.service.LegacyAddOnIdentifierService;
import com.atlassian.plugins.rest.common.Link;
import com.atlassian.plugins.rest.common.security.jersey.SysadminOnlyResourceFilter;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.sun.jersey.spi.container.ResourceFilters;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;

/**
 * REST endpoint which provides a view of Connect add-ons which are installed in the instance.
 */
@ResourceFilters (SysadminOnlyResourceFilter.class)
@Path ("addons")
public class AddonsResource
{
    private static final Logger log = LoggerFactory.getLogger(AddonsResource.class);

    private final PluginAccessor pluginAccessor;
    private final PluginController pluginController;
    private final LegacyAddOnIdentifierService legacyAddOnIdentifierService;
    private final ConnectAddonRegistry addonRegistry;
    private final LicenseRetriever licenseRetriever;
    private final ConnectApplinkManager connectApplinkManager;

    public AddonsResource(PluginAccessor pluginAccessor, PluginController pluginController,
            LegacyAddOnIdentifierService legacyAddOnIdentifierService,
            ConnectAddonRegistry addonRegistry, LicenseRetriever licenseRetriever,
            ConnectApplinkManager connectApplinkManager)
    {
        this.pluginAccessor = pluginAccessor;
        this.pluginController = pluginController;
        this.legacyAddOnIdentifierService = legacyAddOnIdentifierService;
        this.addonRegistry = addonRegistry;
        this.licenseRetriever = licenseRetriever;
        this.connectApplinkManager = connectApplinkManager;
    }

    @GET
    @Produces ("application/json")
    public Response getAddons(@QueryParam ("type") String type)
    {
        try
        {
            RestAddonType addonType = StringUtils.isBlank(type) ? null : RestAddonType.valueOf(type.toUpperCase());
            List<RestAddonStatus> restAddons = getAddonsByType(addonType);
            return Response.ok().entity(restAddons).build();
        }
        catch (IllegalArgumentException e)
        {
            String message = "Type " + type + " is not valid. Valid options: " + Arrays.toString(RestAddonType.values());
            return getErrorResponse(message, Response.Status.BAD_REQUEST);
        }
    }

    @GET
    @Produces ("application/json")
    @Path ("/{addonKey}")
    public Response getAddon(@PathParam ("addonKey") String addonKey)
    {
        RestAddonStatus restAddon = getAddonByKey(addonKey);
        if (restAddon == null)
        {
            String message = "Add-on with key " + addonKey + " was not found";
            return getErrorResponse(message, Response.Status.NOT_FOUND);
        }
        return Response.ok().entity(restAddon).build();
    }

    /**
     * Deletes all XML based Atlassian Connect add-ons from the instance
     */
    @SuppressWarnings ("unchecked")
    @DELETE
    @Path ("/xml")
    @Produces ("application/json")
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

    private List<RestAddonStatus> getAddonsByType(RestAddonType type)
    {
        List<RestAddonStatus> result = Lists.newArrayList();

        if (type == null || type == RestAddonType.XML)
        {
            for (Plugin plugin : getXmlAddonPlugins())
            {
                result.add(createXmlAddonRest(plugin));
            }
        }

        if (type == null || type == RestAddonType.JSON)
        {
            for (ConnectAddonBean addonBean : addonRegistry.getAllAddonBeans())
            {
                result.add(createJsonAddonRest(addonBean));
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

    private RestAddonStatus getAddonByKey(String addonKey)
    {
        for (Plugin plugin : getXmlAddonPlugins())
        {
            if (addonKey.equals(plugin.getKey()))
            {
                return createXmlAddonRest(plugin);
            }
        }

        for (ConnectAddonBean addonBean : addonRegistry.getAllAddonBeans())
        {
            if (addonKey.equals(addonBean.getKey()))
            {
                return createJsonAddonRest(addonBean);
            }
        }

        return null;
    }

    private RestAddonStatus createXmlAddonRest(Plugin plugin)
    {
        String key = plugin.getKey();
        String version = plugin.getPluginInformation().getVersion();
        String state = plugin.getPluginState().name();
        String license = licenseRetriever.getLicenseStatus(key).value();
        RestAddonStatus.AddonApplink appLinkResource = getApplinkResourceForAddon(key);

        return new RestAddonStatus(key, version, RestAddonType.XML, state, license, appLinkResource);
    }

    private RestAddonStatus createJsonAddonRest(ConnectAddonBean addonBean)
    {
        String key = addonBean.getKey();
        String version = addonBean.getVersion();
        String state = addonRegistry.getRestartState(key).name();
        String license = licenseRetriever.getLicenseStatus(key).value();
        RestAddonStatus.AddonApplink appLinkResource = getApplinkResourceForAddon(key);

        return new RestAddonStatus(key, version, RestAddonType.JSON, state, license, appLinkResource);
    }

    private RestAddonStatus.AddonApplink getApplinkResourceForAddon(String key)
    {
        ApplicationLink appLink = connectApplinkManager.getAppLink(key);
        String appLinkId = appLink.getId().get();
        URI selfUri = connectApplinkManager.getApplinkLinkSelfLink(appLink);

        return new RestAddonStatus.AddonApplink(appLinkId, Link.self(selfUri));
    }

    private Response getErrorResponse(final String message, final Response.Status status)
    {
        RestError error = new RestError(status.getStatusCode(), message);
        return Response.status(status)
                .entity(error)
                .build();
    }
}
