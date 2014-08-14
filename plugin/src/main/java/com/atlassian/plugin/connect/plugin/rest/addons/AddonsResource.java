package com.atlassian.plugin.connect.plugin.rest.addons;

import com.atlassian.applinks.api.ApplicationLink;
import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.PluginAccessor;
import com.atlassian.plugin.PluginController;
import com.atlassian.plugin.PluginException;
import com.atlassian.plugin.connect.modules.beans.ConnectAddonBean;
import com.atlassian.plugin.connect.plugin.applinks.ConnectApplinkManager;
import com.atlassian.plugin.connect.plugin.applinks.NotConnectAddonException;
import com.atlassian.plugin.connect.plugin.installer.ConnectAddOnInstaller;
import com.atlassian.plugin.connect.plugin.installer.ConnectAddonManager;
import com.atlassian.plugin.connect.plugin.license.LicenseRetriever;
import com.atlassian.plugin.connect.plugin.registry.ConnectAddonRegistry;
import com.atlassian.plugin.connect.plugin.rest.RestError;
import com.atlassian.plugin.connect.plugin.rest.data.RestAddon;
import com.atlassian.plugin.connect.plugin.rest.data.RestAddonType;
import com.atlassian.plugin.connect.plugin.rest.data.RestAddons;
import com.atlassian.plugin.connect.plugin.rest.data.RestMinimalAddon;
import com.atlassian.plugin.connect.plugin.rest.data.RestNamedLink;
import com.atlassian.plugin.connect.plugin.rest.data.RestRelatedLinks;
import com.atlassian.plugin.connect.plugin.service.LegacyAddOnIdentifierService;
import com.atlassian.plugins.rest.common.Link;
import com.atlassian.plugins.rest.common.security.jersey.SysadminOnlyResourceFilter;
import com.atlassian.sal.api.ApplicationProperties;
import com.atlassian.sal.api.UrlMode;
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
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;

/**
 * REST endpoint which provides a view of Connect add-ons which are installed in the instance.
 */
@ResourceFilters (SysadminOnlyResourceFilter.class)
@Path (AddonsResource.REST_PATH)
public class AddonsResource
{
    public final static String REST_PATH = "addons";

    private static final Logger log = LoggerFactory.getLogger(AddonsResource.class);

    private final PluginAccessor pluginAccessor;
    private final PluginController pluginController;
    private final LegacyAddOnIdentifierService legacyAddOnIdentifierService;
    private final ConnectAddonRegistry addonRegistry;
    private final LicenseRetriever licenseRetriever;
    private final ConnectApplinkManager connectApplinkManager;
    private final ConnectAddonManager connectAddonManager;
    private final ConnectAddOnInstaller connectAddOnInstaller;
    private final ApplicationProperties applicationProperties;

    public AddonsResource(PluginAccessor pluginAccessor, PluginController pluginController,
            LegacyAddOnIdentifierService legacyAddOnIdentifierService,
            ConnectAddonRegistry addonRegistry, LicenseRetriever licenseRetriever,
            ConnectApplinkManager connectApplinkManager, ConnectAddonManager connectAddonManager,
            ConnectAddOnInstaller connectAddOnInstaller, ApplicationProperties applicationProperties)
    {
        this.pluginAccessor = pluginAccessor;
        this.pluginController = pluginController;
        this.legacyAddOnIdentifierService = legacyAddOnIdentifierService;
        this.addonRegistry = addonRegistry;
        this.licenseRetriever = licenseRetriever;
        this.connectApplinkManager = connectApplinkManager;
        this.connectAddonManager = connectAddonManager;
        this.connectAddOnInstaller = connectAddOnInstaller;
        this.applicationProperties = applicationProperties;
    }

    @GET
    @Produces ("application/json")
    public Response getAddons(@QueryParam ("type") String type)
    {
        try
        {
            RestAddonType addonType = StringUtils.isBlank(type) ? null : RestAddonType.valueOf(type.toUpperCase());
            RestAddons restAddons = getAddonsByType(addonType);
            return Response.ok().entity(restAddons).build();
        }
        catch (IllegalArgumentException e)
        {
            String message = "Type " + type + " is not valid. Valid options: " + Arrays.toString(RestAddonType.values());
            return getErrorResponse(message, Response.Status.BAD_REQUEST);
        }
    }

    @SuppressWarnings ("unchecked")
    @DELETE
    @Produces ("application/json")
    public Response uninstallAddons(@QueryParam ("type") String type)
    {
        RestAddonType addonType = null;
        if (StringUtils.isNotBlank(type))
        {
            try
            {
                addonType = RestAddonType.valueOf(type.toUpperCase());
            }
            catch (IllegalArgumentException e)
            {
                String message = "Type " + type + " is not valid. Valid options: " + Arrays.toString(RestAddonType.values());
                return getErrorResponse(message, Response.Status.BAD_REQUEST);
            }
        }

        // Later we may support otherwise
        if (addonType != RestAddonType.XML)
        {
            String message = "Only bulk uninstall of XML add-ons are supported";
            log.error(message);
            return getErrorResponse(message, Response.Status.FORBIDDEN);
        }

        log.warn("Uninstalling all Connect add-ons of type " + addonType);

        List<RestMinimalAddon> addons = Lists.newArrayList();
        List<PluginException> errors = Lists.newArrayList();

        Map result = Maps.newHashMap();
        List<Plugin> connectAddonPlugins;

        switch (addonType)
        {
            case XML:
                connectAddonPlugins = getXmlAddonPlugins();
                break;

            case JSON:
            default:
                throw new UnsupportedOperationException("Not sure how to return a JSON add-on's plugin or uninstall it");
        }

        for (Plugin plugin : connectAddonPlugins)
        {
            try
            {
                RestMinimalAddon addon = uninstallPlugin(plugin);
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
    @Produces ("application/json")
    @Path ("/{addonKey}")
    public Response getAddon(@PathParam ("addonKey") String addonKey)
    {
        RestAddon restAddon = getRestAddonByKey(addonKey);
        if (restAddon == null)
        {
            String message = "Add-on with key " + addonKey + " was not found";
            return getErrorResponse(message, Response.Status.NOT_FOUND);
        }
        return Response.ok().entity(restAddon).build();
    }

    @DELETE
    @Produces ("application/json")
    @Path ("/{addonKey}")
    public Response uninstallAddon(@PathParam ("addonKey") String addonKey)
    {
        try
        {
            for (Plugin plugin : getXmlAddonPlugins())
            {
                if (addonKey.equals(plugin.getKey()))
                {
                    RestMinimalAddon addon = uninstallPlugin(plugin);
                    return Response.ok().entity(addon).build();
                }
            }

            ConnectAddonBean addonBean = connectAddonManager.getExistingAddon(addonKey);
            if (addonBean != null)
            {
                RestMinimalAddon addon = new RestMinimalAddon(addonKey, addonBean.getVersion(), RestAddonType.JSON);
                connectAddonManager.uninstallConnectAddon(addonKey);
                return Response.ok().entity(addon).build();
            }
        }
        catch (Exception e)
        {
            String message = "Unable to uninstall add-on " + addonKey + ": " + e.getMessage();
            log.error(message, e);
            return getErrorResponse(message, Response.Status.INTERNAL_SERVER_ERROR);
        }

        String message = "Add-on with key " + addonKey + " was not found";
        return getErrorResponse(message, Response.Status.NOT_FOUND);
    }

    @PUT
    @Produces ("application/json")
    @Path ("/{addonKey}/reinstall")
    public Response reinstallAddon(@PathParam ("addonKey") String addonKey)
    {
        try
        {
            ConnectAddonBean addonBean = connectAddonManager.getExistingAddon(addonKey);
            if (addonBean != null)
            {
                String descriptor = addonRegistry.getDescriptor(addonKey);

                connectAddonManager.uninstallConnectAddonQuietly(addonKey);
                connectAddOnInstaller.install(descriptor);

                RestAddon restAddon = getRestAddonByKey(addonKey);
                return Response.ok().entity(restAddon).build();
            }
        }
        catch (Exception e)
        {
            String message = "Unable to reinstall add-on " + addonKey + ": " + e.getMessage();
            log.error(message, e);
            return getErrorResponse(message, Response.Status.INTERNAL_SERVER_ERROR);
        }

        String message = "Add-on with key " + addonKey + " was not found";
        return getErrorResponse(message, Response.Status.NOT_FOUND);
    }

    private RestAddons getAddonsByType(RestAddonType type)
    {
        List<RestAddon> result = Lists.newArrayList();

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

        return new RestAddons<RestAddon>(result);
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

    private RestAddon getRestAddonByKey(String addonKey)
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

    private RestAddon createXmlAddonRest(Plugin plugin)
    {
        String key = plugin.getKey();
        String version = plugin.getPluginInformation().getVersion();
        String state = plugin.getPluginState().name();
        String license = licenseRetriever.getLicenseStatus(key).value();
        RestAddon.AddonApplink appLinkResource = getApplinkResourceForAddon(key);

        return new RestAddon(key, version, RestAddonType.XML, state, license, appLinkResource, getAddonLinks(key));
    }

    private RestAddon createJsonAddonRest(ConnectAddonBean addonBean)
    {
        String key = addonBean.getKey();
        String version = addonBean.getVersion();
        String state = addonRegistry.getRestartState(key).name();
        String license = licenseRetriever.getLicenseStatus(key).value();
        RestAddon.AddonApplink appLinkResource = getApplinkResourceForAddon(key);

        return new RestAddon(key, version, RestAddonType.JSON, state, license, appLinkResource, getAddonLinks(key));
    }

    private RestRelatedLinks getAddonLinks(String key)
    {
        String path = applicationProperties.getBaseUrl(UrlMode.CANONICAL) + "/rest/atlassian-connect/1/" + REST_PATH + "/" + key;
        RestNamedLink selfLink = new RestNamedLink(path);

        RestNamedLink mpacLink = new RestNamedLink("https://marketplace.atlassian.com/plugins/" + key);

        return new RestRelatedLinks.Builder()
                .addRelatedLink(RestRelatedLinks.RELATIONSHIP_SELF, selfLink)
                .addRelatedLink("marketplace", mpacLink)
                .build();
    }

    private RestAddon.AddonApplink getApplinkResourceForAddon(String key)
    {
        try
        {
            ApplicationLink appLink = connectApplinkManager.getAppLink(key);
            if (appLink == null) {
                log.info("Add-on " + key + " has no applink");
                return null;
            } else if (appLink.getId() == null) {
                log.info("Add-on " + key + " has no applink id");
                return null;
            }
            String appLinkId = appLink.getId().get();
            URI selfUri = connectApplinkManager.getApplinkLinkSelfLink(appLink);

            return new RestAddon.AddonApplink(appLinkId, Link.self(selfUri));
        }
        catch (Exception e)
        {
            log.error("Could not retrieve applink for key " + key);
            return null;
        }
    }

    private RestMinimalAddon uninstallPlugin(Plugin plugin) throws PluginException
    {
        String key = plugin.getKey();
        String version = plugin.getPluginInformation().getVersion();
        RestMinimalAddon addon = new RestMinimalAddon(key, version, RestAddonType.XML);
        pluginController.uninstall(plugin);
        log.warn("Uninstalled add-on " + plugin.getKey());
        return addon;
    }

    private Response getErrorResponse(final String message, final Response.Status status)
    {
        RestError error = new RestError(status.getStatusCode(), message);
        return Response.status(status)
                .entity(error)
                .build();
    }
}
