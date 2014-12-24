package com.atlassian.plugin.connect.plugin.rest.addons;

import com.atlassian.annotations.PublicApi;
import com.atlassian.applinks.api.ApplicationLink;
import com.atlassian.plugin.PluginState;
import com.atlassian.plugin.connect.modules.beans.ConnectAddonBean;
import com.atlassian.plugin.connect.plugin.applinks.ConnectApplinkManager;
import com.atlassian.plugin.connect.plugin.installer.ConnectAddOnInstaller;
import com.atlassian.plugin.connect.plugin.installer.ConnectAddonManager;
import com.atlassian.plugin.connect.plugin.license.LicenseRetriever;
import com.atlassian.plugin.connect.plugin.registry.ConnectAddonRegistry;
import com.atlassian.plugin.connect.plugin.rest.AddonOrSysadminOnlyResourceFilter;
import com.atlassian.plugin.connect.plugin.rest.RestError;
import com.atlassian.plugin.connect.plugin.rest.data.RestAddon;
import com.atlassian.plugin.connect.plugin.rest.data.RestAddonLicense;
import com.atlassian.plugin.connect.plugin.rest.data.RestAddonLicenseMapper;
import com.atlassian.plugin.connect.plugin.rest.data.RestAddons;
import com.atlassian.plugin.connect.plugin.rest.data.RestInternalAddon;
import com.atlassian.plugin.connect.plugin.rest.data.RestLimitedAddon;
import com.atlassian.plugin.connect.plugin.rest.data.RestMinimalAddon;
import com.atlassian.plugin.connect.plugin.rest.data.RestNamedLink;
import com.atlassian.plugin.connect.plugin.rest.data.RestRelatedLinks;
import com.atlassian.plugins.rest.common.Link;
import com.atlassian.plugins.rest.common.security.AnonymousAllowed;
import com.atlassian.plugins.rest.common.security.jersey.SysadminOnlyResourceFilter;
import com.atlassian.sal.api.ApplicationProperties;
import com.atlassian.sal.api.UrlMode;
import com.atlassian.sal.api.user.UserManager;
import com.atlassian.upm.api.license.entity.PluginLicense;
import com.atlassian.upm.api.util.Option;
import com.google.common.collect.Lists;
import com.sun.jersey.spi.container.ResourceFilters;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;
import java.net.URI;
import java.util.List;

import static com.atlassian.plugin.connect.plugin.rest.ConnectRestConstants.ADDON_KEY_PATH_PARAMETER;

/**
 * REST endpoint which provides a view of Connect add-ons which are installed in the instance.
 * <p/>
 * NOTE: This resource class exposes some functionality for add-on developers and some for system administrators.
 */
@Path (AddonsResource.REST_PATH)
public class AddonsResource
{
    public final static String REST_PATH = "addons";

    private static final Logger log = LoggerFactory.getLogger(AddonsResource.class);

    private final ConnectAddonRegistry addonRegistry;
    private final LicenseRetriever licenseRetriever;
    private final ConnectApplinkManager connectApplinkManager;
    private final ConnectAddonManager connectAddonManager;
    private final ConnectAddOnInstaller connectAddOnInstaller;
    private final ApplicationProperties applicationProperties;
    private final UserManager userManager;

    public AddonsResource(ConnectAddonRegistry addonRegistry, LicenseRetriever licenseRetriever,
            ConnectApplinkManager connectApplinkManager, ConnectAddonManager connectAddonManager,
            ConnectAddOnInstaller connectAddOnInstaller, ApplicationProperties applicationProperties,
            UserManager userManager)
    {
        this.addonRegistry = addonRegistry;
        this.licenseRetriever = licenseRetriever;
        this.connectApplinkManager = connectApplinkManager;
        this.connectAddonManager = connectAddonManager;
        this.connectAddOnInstaller = connectAddOnInstaller;
        this.applicationProperties = applicationProperties;
        this.userManager = userManager;
    }

    @GET
    @ResourceFilters(SysadminOnlyResourceFilter.class)
    @Produces ("application/json")
    public Response getAddons()
    {
        RestAddons restAddons = getAddonResources();
        return Response.ok().entity(restAddons).build();
    }

    /**
     * Returns the add-on with the given key.
     *
     * @param addonKey the key of the add-on, as defined in its descriptor
     * @return a JSON representation of the add-on
     */
    @GET
    @Path ("/{" + ADDON_KEY_PATH_PARAMETER + "}")
    @ResourceFilters(AddonOrSysadminOnlyResourceFilter.class)
    @AnonymousAllowed
    @Produces ("application/json")
    @PublicApi
    public Response getAddon(@PathParam (ADDON_KEY_PATH_PARAMETER) String addonKey)
    {
        RestMinimalAddon restAddon = getRestAddonByKey(addonKey);
        if (restAddon == null)
        {
            String message = "Add-on with key " + addonKey + " was not found";
            return getErrorResponse(message, Response.Status.NOT_FOUND);
        }
        return Response.ok().entity(restAddon).build();
    }

    @DELETE
    @Path ("/{" + ADDON_KEY_PATH_PARAMETER + "}")
    @ResourceFilters(SysadminOnlyResourceFilter.class)
    @Produces ("application/json")
    public Response uninstallAddon(@PathParam (ADDON_KEY_PATH_PARAMETER) String addonKey)
    {
        try
        {
            ConnectAddonBean addonBean = connectAddonManager.getExistingAddon(addonKey);
            if (addonBean != null)
            {
                RestMinimalAddon addon = new RestMinimalAddon(addonKey, addonBean.getVersion());
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
    @Path ("/{" + ADDON_KEY_PATH_PARAMETER + "}/reinstall")
    @ResourceFilters(SysadminOnlyResourceFilter.class)
    @Produces ("application/json")
    public Response reinstallAddon(@PathParam (ADDON_KEY_PATH_PARAMETER) String addonKey)
    {
        try
        {
            ConnectAddonBean addonBean = connectAddonManager.getExistingAddon(addonKey);
            if (addonBean != null)
            {
                String descriptor = addonRegistry.getDescriptor(addonKey);

                connectAddonManager.uninstallConnectAddonQuietly(addonKey);
                connectAddOnInstaller.install(descriptor);

                RestMinimalAddon restAddon = getRestAddonByKey(addonKey);
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

    private RestAddons getAddonResources()
    {
        List<RestMinimalAddon> result = Lists.newArrayList();
        for (ConnectAddonBean addonBean : addonRegistry.getAllAddonBeans())
        {
            result.add(createJsonAddonRest(addonBean));
        }
        return new RestAddons<RestMinimalAddon>(result);
    }

    private RestMinimalAddon getRestAddonByKey(String addonKey)
    {
        RestMinimalAddon restAddon = null;
        for (ConnectAddonBean addonBean : addonRegistry.getAddonBean(addonKey))
        {
            restAddon = createJsonAddonRest(addonBean);
        }
        return restAddon;
    }

    private RestLimitedAddon createJsonAddonRest(ConnectAddonBean addonBean)
    {
        String key = addonBean.getKey();
        String version = addonBean.getVersion();
        PluginState state = addonRegistry.getRestartState(key);
        String stateString = state.name();
        RestAddonLicense license = getLicenseResourceForAddon(key);
        RestRelatedLinks addonLinks = getAddonLinks(key);
        RestInternalAddon.AddonApplink appLinkResource = getApplinkResourceForAddon(key);

        RestLimitedAddon resource;
        if (userManager.isSystemAdmin(userManager.getRemoteUserKey()))
        {
            resource = new RestInternalAddon(key, version, stateString, license, addonLinks, appLinkResource);
        }
        else
        {
            if (state.equals(PluginState.DISABLED) || state.equals(PluginState.DISABLING))
            {
                resource = new RestLimitedAddon(key, version, stateString);
            }
            else
            {
                resource = new RestAddon(key, version, stateString, license, addonLinks);
            }
        }
        return resource;
    }

    private RestAddonLicense getLicenseResourceForAddon(String key)
    {
        Option<PluginLicense> licenseOption = licenseRetriever.getLicense(key);
        RestAddonLicenseMapper mapper = new RestAddonLicenseMapper();
        RestAddonLicense resource = null;
        for (PluginLicense license : licenseOption)
        {
            resource = mapper.getRestAddonLicense(license);
        }
        return resource;
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

    private RestInternalAddon.AddonApplink getApplinkResourceForAddon(String key)
    {
        try
        {
            ApplicationLink appLink = connectApplinkManager.getAppLink(key);
            if (appLink == null)
            {
                log.info("Add-on " + key + " has no applink");
                return null;
            }
            else if (appLink.getId() == null)
            {
                log.info("Add-on " + key + " has no applink id");
                return null;
            }
            String appLinkId = appLink.getId().get();
            URI selfUri = connectApplinkManager.getApplinkLinkSelfLink(appLink);

            return new RestInternalAddon.AddonApplink(appLinkId, Link.self(selfUri));
        }
        catch (Exception e)
        {
            log.error("Could not retrieve applink for key " + key);
            return null;
        }
    }

    private Response getErrorResponse(final String message, final Response.Status status)
    {
        RestError error = new RestError(status.getStatusCode(), message);
        return Response.status(status)
                .entity(error)
                .build();
    }
}
