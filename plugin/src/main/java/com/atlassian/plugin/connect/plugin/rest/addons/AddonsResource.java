package com.atlassian.plugin.connect.plugin.rest.addons;

import com.atlassian.applinks.api.ApplicationLink;
import com.atlassian.fugue.Either;
import com.atlassian.fugue.Option;
import com.atlassian.plugin.connect.modules.beans.ConnectAddonBean;
import com.atlassian.plugin.connect.plugin.ao.AddOnProperty;
import com.atlassian.plugin.connect.plugin.ao.AddOnPropertyIterable;
import com.atlassian.plugin.connect.plugin.applinks.ConnectApplinkManager;
import com.atlassian.plugin.connect.plugin.installer.ConnectAddOnInstaller;
import com.atlassian.plugin.connect.plugin.installer.ConnectAddonManager;
import com.atlassian.plugin.connect.plugin.license.LicenseRetriever;
import com.atlassian.plugin.connect.plugin.registry.ConnectAddonRegistry;
import com.atlassian.plugin.connect.plugin.rest.RestResult;
import com.atlassian.plugin.connect.plugin.rest.data.ETag;
import com.atlassian.plugin.connect.plugin.rest.data.RestAddOnPropertiesBean;
import com.atlassian.plugin.connect.plugin.rest.data.RestAddOnProperty;
import com.atlassian.plugin.connect.plugin.rest.data.RestAddon;
import com.atlassian.plugin.connect.plugin.rest.data.RestAddonType;
import com.atlassian.plugin.connect.plugin.rest.data.RestAddons;
import com.atlassian.plugin.connect.plugin.rest.data.RestMinimalAddon;
import com.atlassian.plugin.connect.plugin.rest.data.RestNamedLink;
import com.atlassian.plugin.connect.plugin.rest.data.RestRelatedLinks;
import com.atlassian.plugin.connect.plugin.scopes.AddOnKeyExtractor;
import com.atlassian.plugin.connect.plugin.service.AddOnPropertyService;
import com.atlassian.plugin.connect.plugin.service.AddOnPropertyServiceImpl;
import com.atlassian.plugins.rest.common.Link;
import com.atlassian.sal.api.ApplicationProperties;
import com.atlassian.sal.api.UrlMode;
import com.atlassian.sal.api.user.UserManager;
import com.atlassian.sal.api.user.UserProfile;
import com.google.common.base.Function;
import com.google.common.base.Optional;
import com.google.common.collect.Lists;
import com.google.common.io.LimitInputStream;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URI;
import java.nio.charset.Charset;
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
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;

import static com.atlassian.plugin.connect.plugin.service.AddOnPropertyService.ServiceResult;

/**
 * REST endpoint which provides a view of Connect add-ons which are installed in the instance.
 */
@Path (AddonsResource.REST_PATH)
@Produces ("application/json")
@Consumes ("application/json")
public class AddonsResource
{
    public final static String REST_PATH = "addons";

    private static final Logger log = LoggerFactory.getLogger(AddonsResource.class);
    public static final String VALUE_TOO_LONG_ERROR_MSG = String.format("The value cannot be bigger than %s.", FileUtils.byteCountToDisplaySize(AddOnPropertyServiceImpl.MAXIMUM_PROPERTY_VALUE_LENGTH));

    private final ConnectAddonRegistry addonRegistry;
    private final LicenseRetriever licenseRetriever;
    private final ConnectApplinkManager connectApplinkManager;
    private final ConnectAddonManager connectAddonManager;
    private final ConnectAddOnInstaller connectAddOnInstaller;
    private final ApplicationProperties applicationProperties;
    private final AddOnPropertyService addOnPropertyService;
    private final AddOnKeyExtractor addOnKeyExtractor;
    private final UserManager userManager;

    public AddonsResource(ConnectAddonRegistry addonRegistry, LicenseRetriever licenseRetriever,
            ConnectApplinkManager connectApplinkManager, ConnectAddonManager connectAddonManager,
            ConnectAddOnInstaller connectAddOnInstaller, ApplicationProperties applicationProperties,
            AddOnPropertyService addOnPropertyService, AddOnKeyExtractor addOnKeyExtractor, UserManager userManager)
    {
        this.addonRegistry = addonRegistry;
        this.licenseRetriever = licenseRetriever;
        this.connectApplinkManager = connectApplinkManager;
        this.connectAddonManager = connectAddonManager;
        this.connectAddOnInstaller = connectAddOnInstaller;
        this.applicationProperties = applicationProperties;
        this.addOnPropertyService = addOnPropertyService;
        this.addOnKeyExtractor = addOnKeyExtractor;
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

    @GET
    @Path ("{addonKey}/properties")
    public Response getAddOnProperties(@PathParam ("addonKey") final String addOnKey, @Context HttpServletRequest request)
    {
        UserProfile user = userManager.getRemoteUser(request);
        ETag eTag = getETagFromRequest(request);
        String sourcePluginKey = addOnKeyExtractor.getAddOnKeyFromHttpRequest(request);

        Either<ServiceResult, AddOnPropertyIterable> result = addOnPropertyService.getAddOnProperties(user, sourcePluginKey, addOnKey, eTag);

        return result.fold(new Function<ServiceResult, Response>()
        {
            @Override
            public Response apply(final ServiceResult input)
            {
                return getResponseFromServiceResult(input);
            }
        }, new Function<AddOnPropertyIterable, Response>()
        {
            @Override
            public Response apply(final AddOnPropertyIterable input)
            {
                String baseURL = getRestPathForAddOnKey(addOnKey) + "/properties";
                return Response.ok()
                        .entity(RestAddOnPropertiesBean.valueOf(input.getPropertyKeys(), baseURL))
                        .tag(input.getETag().toString())
                        .cacheControl(never())
                        .build();
            }
        });
    }

    @GET
    @Path ("{addonKey}/properties/{propertyKey}")
    public Response getAddOnProperty(@PathParam ("addonKey") final String addOnKey, @PathParam("propertyKey") String propertyKey, @Context HttpServletRequest request)
    {
        UserProfile user = userManager.getRemoteUser(request);
        ETag eTag = getETagFromRequest(request);
        String sourcePluginKey = addOnKeyExtractor.getAddOnKeyFromHttpRequest(request);

        Either<ServiceResult, AddOnProperty> propertyValue = addOnPropertyService.getPropertyValue(user, sourcePluginKey, addOnKey, propertyKey, eTag);

        return propertyValue.fold(new Function<ServiceResult, Response>()
        {
            @Override
            public Response apply(final ServiceResult input)
            {
                return getResponseFromServiceResult(input);
            }
        }, new Function<AddOnProperty, Response>()
        {
            @Override
            public Response apply(final AddOnProperty input)
            {
                String baseURL = getRestPathForAddOnKey(addOnKey) + "/properties";
                return Response.ok().entity(RestAddOnProperty.valueOf(input, baseURL)).tag(input.getETag().toString()).cacheControl(never()).build();
            }
        });
    }

    private ETag getETagFromRequest(final HttpServletRequest request)
    {
        return new ETag(request.getHeader(HttpHeaders.IF_MATCH));
    }

    @PUT
    @Path ("{addonKey}/properties/{propertyKey}")
    public Response putAddOnProperty(@PathParam ("addonKey") final String addOnKey, @PathParam("propertyKey") final String propertyKey, @Context HttpServletRequest request)
    {
        final UserProfile user = userManager.getRemoteUser(request);
        // can be null, it is checked in the service.
        final String sourcePluginKey = addOnKeyExtractor.getAddOnKeyFromHttpRequest(request);
        final ETag eTag = getETagFromRequest(request);

        Either<RestParamError, String> errorStringEither = propertyValue(request);
        return errorStringEither.fold(new Function<RestParamError, Response>()
        {
            @Override
            public Response apply(final RestParamError input)
            {
                return getResponseForMessageAndStatus(VALUE_TOO_LONG_ERROR_MSG, Response.Status.FORBIDDEN);
            }
        }, new Function<String, Response>()
        {
            @Override
            public Response apply(final String propertyValue)
            {
                ServiceResult serviceResult = addOnPropertyService.setPropertyValue(user, sourcePluginKey, addOnKey, propertyKey, propertyValue, eTag);
                Response responseFromServiceResult = getResponseFromServiceResult(serviceResult);
                if (shouldReturnETag(serviceResult))
                {
                    return getResponseFromServiceResult(serviceResult, Option.some(new AddOnProperty(propertyKey, propertyValue).getETag().toString()));
                }
                return responseFromServiceResult;
            }
        });
    }

    private boolean shouldReturnETag(final ServiceResult serviceResult)
    {
        switch (serviceResult.getHttpStatusCode()) {
            case HttpStatus.SC_CREATED:
            case HttpStatus.SC_OK:
                return true;
        }
        return false;
    }

    @DELETE
    @Path ("{addonKey}/properties/{propertyKey}")
    public Response deleteAddOnProperty(@PathParam ("addonKey") final String addOnKey, @PathParam("propertyKey") final String propertyKey, @Context HttpServletRequest request)
    {
        final UserProfile user = userManager.getRemoteUser(request);
        // can be null, it is checked in the service.
        final String sourcePluginKey = addOnKeyExtractor.getAddOnKeyFromHttpRequest(request);

        ServiceResult serviceResult = addOnPropertyService.deletePropertyValue(user, sourcePluginKey, addOnKey, propertyKey);
        return getResponseFromServiceResult(serviceResult);
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

    private Response getResponseForMessageAndStatus(final String message, final Response.Status status)
    {
        return getResponseForMessageAndStatus(message, status, Option.<String>none());
    }

    private Response getResponseForMessageAndStatus(final String message, final Response.Status status, final Option<String> eTag)
    {
        RestResult result = new RestResult(status.getStatusCode(), message);
        Response.ResponseBuilder responseBuilder = Response.status(status);
        if (status != Response.Status.NO_CONTENT)
        {
            responseBuilder.entity(result);
        }
        if (eTag.isDefined())
        {
            responseBuilder.tag(eTag.get());
        }
        return responseBuilder
                .cacheControl(never())
                .build();
    }

    private Response getResponseFromServiceResult(final ServiceResult serviceResult)
    {
        return getResponseForMessageAndStatus(serviceResult.message(), Response.Status.fromStatusCode(serviceResult.getHttpStatusCode()), Option.<String>none());
    }

    private Response getResponseFromServiceResult(final ServiceResult serviceResult, final Option<String> eTag)
    {
        return getResponseForMessageAndStatus(serviceResult.message(), Response.Status.fromStatusCode(serviceResult.getHttpStatusCode()), eTag);
    }

    private Either<RestParamError, String> propertyValue(final HttpServletRequest request)
    {
        try
        {
            LimitInputStream limitInputStream =
                    new LimitInputStream(request.getInputStream(), AddOnPropertyServiceImpl.MAXIMUM_PROPERTY_VALUE_LENGTH + 1);
            byte[] bytes = IOUtils.toByteArray(limitInputStream);
            if (bytes.length > AddOnPropertyServiceImpl.MAXIMUM_PROPERTY_VALUE_LENGTH)
            {
                return Either.left(RestParamError.PROPERTY_VALUE_TOO_LONG);
            }
            return Either.right(new String(bytes, Charset.defaultCharset()));
        }
        catch (IOException e)
        {
            return Either.left(RestParamError.PROPERTY_VALUE_TOO_LONG);
        }
    }

    private enum RestParamError
    {
        PROPERTY_VALUE_TOO_LONG
    }

    private static CacheControl never()
    {
        CacheControl cacheNever = new CacheControl();
        cacheNever.setNoStore(true);
        cacheNever.setNoCache(true);

        return cacheNever;
    }

}
