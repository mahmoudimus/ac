package com.atlassian.plugin.connect.plugin.rest.addons;

import com.atlassian.applinks.api.ApplicationLink;
import com.atlassian.plugin.connect.modules.beans.ConnectAddonBean;
import com.atlassian.plugin.connect.plugin.applinks.ConnectApplinkManager;
import com.atlassian.plugin.connect.plugin.installer.ConnectAddOnInstaller;
import com.atlassian.plugin.connect.plugin.installer.ConnectAddonManager;
import com.atlassian.plugin.connect.plugin.license.LicenseRetriever;
import com.atlassian.plugin.connect.plugin.registry.ConnectAddonRegistry;
import com.atlassian.plugin.connect.plugin.rest.RestResult;
import com.atlassian.plugin.connect.plugin.rest.data.RestAddon;
import com.atlassian.plugin.connect.plugin.rest.data.RestAddonType;
import com.atlassian.plugin.connect.plugin.rest.data.RestAddons;
import com.atlassian.plugin.connect.plugin.rest.data.RestMinimalAddon;
import com.atlassian.plugin.connect.plugin.rest.data.RestNamedLink;
import com.atlassian.plugin.connect.plugin.rest.data.RestRelatedLinks;
import com.atlassian.plugins.rest.common.Link;
import com.atlassian.sal.api.ApplicationProperties;
import com.atlassian.sal.api.UrlMode;
import com.atlassian.sal.api.user.UserManager;
import com.atlassian.sal.api.user.UserProfile;
import com.google.common.base.Optional;
import com.google.common.collect.Lists;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.util.Arrays;
import java.util.List;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.CacheControl;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;

/**
 * REST endpoint which provides a view of Connect add-ons which are installed in the instance.
 */
@Path (AddonsResource.REST_PATH)
@Produces ("application/json")
@Consumes ("application/json")
public class AddonsResource
{
    public static final String REST_PATH = "addons";

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
    public Response getAddons(@QueryParam ("type") String type, @Context HttpServletRequest request)
    {
        Optional<Response> errorResponse = checkIfSystemAdmin();
        if (errorResponse.isPresent())
        {
            return errorResponse.get();
        }
        try
        {
            RestAddonType addonType = StringUtils.isBlank(type) ? null : RestAddonType.valueOf(type.toUpperCase());
            RestAddons restAddons = getAddonsByType(addonType);
            return Response.ok().entity(restAddons).build();
        }
        catch (IllegalArgumentException e)
        {
            String message = "Type " + type + " is not valid. Valid options: " + Arrays.toString(RestAddonType.values());
            return getResponseForMessageAndStatus(message, Response.Status.BAD_REQUEST);
        }
    }

    private Optional<Response> checkIfSystemAdmin()
    {
        UserProfile user = userManager.getRemoteUser();
        if (user == null)
        {
            return Optional.of(getResponseForMessageAndStatus("Client must be authenticated to access this resource.", Response.Status.UNAUTHORIZED));
        }
        if (!userManager.isSystemAdmin(user.getUserKey()))
        {
            return Optional.of(getResponseForMessageAndStatus("Client must be authenticated as a system administrator to access this resource.", Response.Status.FORBIDDEN));
        }
        return Optional.absent();
    }

    @GET
    @Path ("/{addonKey}")
    public Response getAddon(@PathParam ("addonKey") String addonKey)
    {
        Optional<Response> errorResponse = checkIfSystemAdmin();
        if (errorResponse.isPresent())
        {
            return errorResponse.get();
        }
        RestAddon restAddon = getRestAddonByKey(addonKey);
        if (restAddon == null)
        {
            String message = "Add-on with key " + addonKey + " was not found";
            return getResponseForMessageAndStatus(message, Response.Status.NOT_FOUND);
        }
        return Response.ok().entity(restAddon).build();
    }

    @DELETE
    @Path ("/{addonKey}")
    public Response uninstallAddon(@PathParam ("addonKey") String addonKey)
    {
        Optional<Response> errorResponse = checkIfSystemAdmin();
        if (errorResponse.isPresent())
        {
            return errorResponse.get();
        }
        try
        {
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
            return getResponseForMessageAndStatus(message, Response.Status.INTERNAL_SERVER_ERROR);
        }

        String message = "Add-on with key " + addonKey + " was not found";
        return getResponseForMessageAndStatus(message, Response.Status.NOT_FOUND);
    }

    @PUT
    @Path ("/{addonKey}/reinstall")
    public Response reinstallAddon(@PathParam ("addonKey") String addonKey)
    {
        Optional<Response> errorResponse = checkIfSystemAdmin();
        if (errorResponse.isPresent())
        {
            return errorResponse.get();
        }
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
            return getResponseForMessageAndStatus(message, Response.Status.INTERNAL_SERVER_ERROR);
        }

        String message = "Add-on with key " + addonKey + " was not found";
        return getResponseForMessageAndStatus(message, Response.Status.NOT_FOUND);
    }

    private RestAddons getAddonsByType(RestAddonType type)
    {
        List<RestAddon> result = Lists.newArrayList();

        if (type == null || type == RestAddonType.JSON)
        {
            for (ConnectAddonBean addonBean : addonRegistry.getAllAddonBeans())
            {
                result.add(createJsonAddonRest(addonBean));
            }
        }

        return new RestAddons<RestAddon>(result);
    }

    private RestAddon getRestAddonByKey(String addonKey)
    {
        for (ConnectAddonBean addonBean : addonRegistry.getAllAddonBeans())
        {
            if (addonKey.equals(addonBean.getKey()))
            {
                return createJsonAddonRest(addonBean);
            }
        }

        return null;
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
        RestNamedLink selfLink = new RestNamedLink(getRestPathForAddOnKey(key));

        RestNamedLink mpacLink = new RestNamedLink("https://marketplace.atlassian.com/plugins/" + key);

        return new RestRelatedLinks.Builder()
                .addRelatedLink(RestRelatedLinks.RELATIONSHIP_SELF, selfLink)
                .addRelatedLink("marketplace", mpacLink)
                .build();
    }

    private String getRestPathForAddOnKey(final String key)
    {
        return applicationProperties.getBaseUrl(UrlMode.CANONICAL) + "/rest/atlassian-connect/1/" + REST_PATH + "/" + key;
    }

    private RestAddon.AddonApplink getApplinkResourceForAddon(String key)
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

            return new RestAddon.AddonApplink(appLinkId, Link.self(selfUri));
        }
        catch (Exception e)
        {
            log.error("Could not retrieve applink for key " + key);
            return null;
        }
    }

    private Response.ResponseBuilder getResponseBuilderForMessageAndStatus(final String message, final Response.Status status)
    {
        RestResult result = new RestResult(status.getStatusCode(), message);
        Response.ResponseBuilder responseBuilder = Response.status(status);
        if (status != Response.Status.NO_CONTENT)
        {
            responseBuilder.entity(result);
        }
        return responseBuilder.cacheControl(never());
    }

    private Response getResponseForMessageAndStatus(final String message, final Response.Status status)
    {
        return getResponseBuilderForMessageAndStatus(message, status).build();
    }

    private static CacheControl never()
    {
        CacheControl cacheNever = new CacheControl();
        cacheNever.setNoStore(true);
        cacheNever.setNoCache(true);

        return cacheNever;
    }

}
