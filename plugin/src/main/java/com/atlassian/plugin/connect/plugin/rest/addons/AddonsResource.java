package com.atlassian.plugin.connect.plugin.rest.addons;

import com.atlassian.applinks.api.ApplicationLink;
import com.atlassian.jira.entity.property.JsonEntityPropertyManagerImpl;
import com.atlassian.plugin.connect.modules.beans.ConnectAddonBean;
import com.atlassian.plugin.connect.plugin.ao.AddOnProperty;
import com.atlassian.plugin.connect.plugin.applinks.ConnectApplinkManager;
import com.atlassian.plugin.connect.plugin.installer.ConnectAddOnInstaller;
import com.atlassian.plugin.connect.plugin.installer.ConnectAddonManager;
import com.atlassian.plugin.connect.plugin.license.LicenseRetriever;
import com.atlassian.plugin.connect.plugin.registry.ConnectAddonRegistry;
import com.atlassian.plugin.connect.plugin.rest.RestError;
import com.atlassian.plugin.connect.plugin.rest.data.RestAddon;
import com.atlassian.plugin.connect.plugin.rest.data.RestAddonProperty;
import com.atlassian.plugin.connect.plugin.rest.data.RestAddonType;
import com.atlassian.plugin.connect.plugin.rest.data.RestAddons;
import com.atlassian.plugin.connect.plugin.rest.data.RestMinimalAddon;
import com.atlassian.plugin.connect.plugin.rest.data.RestNamedLink;
import com.atlassian.plugin.connect.plugin.rest.data.RestRelatedLinks;
import com.atlassian.plugin.connect.plugin.service.AddOnPropertyService;
import com.atlassian.plugin.connect.plugin.service.AddOnPropertyServiceImpl;
import com.atlassian.plugins.rest.common.Link;
import com.atlassian.plugins.rest.common.security.jersey.SysadminOnlyResourceFilter;
import com.atlassian.sal.api.ApplicationProperties;
import com.atlassian.sal.api.UrlMode;
import com.google.common.collect.Lists;
import com.google.common.io.LimitInputStream;
import com.sun.jersey.spi.container.ResourceFilters;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.net.URI;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.List;

/**
 * REST endpoint which provides a view of Connect add-ons which are installed in the instance.
 */
@ResourceFilters (SysadminOnlyResourceFilter.class)
@Path (AddonsResource.REST_PATH)
@Produces ("application/json")
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
    private final AddOnPropertyService addOnPropertyService;

    public AddonsResource(ConnectAddonRegistry addonRegistry, LicenseRetriever licenseRetriever,
            ConnectApplinkManager connectApplinkManager, ConnectAddonManager connectAddonManager,
            ConnectAddOnInstaller connectAddOnInstaller, ApplicationProperties applicationProperties,
            AddOnPropertyService addOnPropertyService)
    {
        this.addonRegistry = addonRegistry;
        this.licenseRetriever = licenseRetriever;
        this.connectApplinkManager = connectApplinkManager;
        this.connectAddonManager = connectAddonManager;
        this.connectAddOnInstaller = connectAddOnInstaller;
        this.applicationProperties = applicationProperties;
        this.addOnPropertyService = addOnPropertyService;
    }

    @GET
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

    @GET
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
    @Path ("/{addonKey}")
    public Response uninstallAddon(@PathParam ("addonKey") String addonKey)
    {
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
            return getErrorResponse(message, Response.Status.INTERNAL_SERVER_ERROR);
        }

        String message = "Add-on with key " + addonKey + " was not found";
        return getErrorResponse(message, Response.Status.NOT_FOUND);
    }

    @PUT
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

    @GET
    @Path ("/{addonKey}/properties/{key}")
    public Response getAddonProperties(@PathParam ("addonKey") String addonKey, @PathParam("key") String propertyKey)
    {
        RestAddon restAddon = getRestAddonByKey(addonKey);
        if (restAddon == null)
        {
            String message = "Add-on with key " + addonKey + " was not found";
            return getErrorResponse(message, Response.Status.NOT_FOUND);
        }
        AddOnProperty addonProperty = addOnPropertyService.getPropertyValue(addonKey, propertyKey);
        if (addonProperty == null)
        {
            String message = "Property for key " + propertyKey + " was not found";
            return getErrorResponse(message, Response.Status.NOT_FOUND);
        }

        return Response.ok().entity(RestAddonProperty.valueOf(addonProperty)).build();

    }



    @PUT
    @Path ("/{addonKey}/properties/{key}")
    public Response putKey(@PathParam ("addonKey") String addonKey, @PathParam("key") String propertyKey, @Context final HttpServletRequest request)
    {
        RestAddon restAddon = getRestAddonByKey(addonKey);
        if (restAddon == null)
        {
            String message = "Add-on with key " + addonKey + " was not found";
            return getErrorResponse(message, Response.Status.NOT_FOUND);
        }

        addOnPropertyService.setPropertyValue(addonKey,propertyKey,propertyValue(request));
        return Response.ok().build();
    }

    private String propertyValue(final HttpServletRequest request)
    {
        try
        {
            LimitInputStream limitInputStream =
                    new LimitInputStream(request.getInputStream(), JsonEntityPropertyManagerImpl.MAXIMUM_VALUE_LENGTH + 1);
            byte[] bytes = IOUtils.toByteArray(limitInputStream);
            if (bytes.length > AddOnPropertyServiceImpl.MAXIMUM_VALUE_LENGTH)
            {
                return null;
            }
            return new String(bytes, Charset.defaultCharset());//forName(ComponentAccessor.getApplicationProperties().getEncoding()));
        }
        catch (IOException e)
        {
            return null;
            //throw new BadRequestWebException(ErrorCollection.of(e.getMessage()));
        }
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

    private Response getErrorResponse(final String message, final Response.Status status)
    {
        RestError error = new RestError(status.getStatusCode(), message);
        return Response.status(status)
                .entity(error)
                .build();
    }
}
