package com.atlassian.plugin.connect.plugin.rest.addons;

import java.io.IOException;
import java.util.Optional;

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
import javax.ws.rs.core.Request;
import javax.ws.rs.core.Response;

import com.atlassian.fugue.Either;
import com.atlassian.plugin.connect.api.auth.scope.AddonKeyExtractor;
import com.atlassian.plugin.connect.plugin.property.AddonProperty;
import com.atlassian.plugin.connect.plugin.property.AddonPropertyIterable;
import com.atlassian.plugin.connect.plugin.property.AddonPropertyService;
import com.atlassian.plugin.connect.plugin.property.AddonPropertyServiceImpl;
import com.atlassian.plugin.connect.plugin.rest.RestResult;
import com.atlassian.plugin.connect.plugin.rest.data.RestAddonPropertiesBean;
import com.atlassian.plugin.connect.plugin.rest.data.RestAddonProperty;
import com.atlassian.sal.api.ApplicationProperties;
import com.atlassian.sal.api.UrlMode;
import com.atlassian.sal.api.message.I18nResolver;
import com.atlassian.sal.api.user.UserManager;
import com.atlassian.sal.api.user.UserProfile;

import com.google.common.base.Function;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * REST endpoint for add-on properties
 */
@Path (AddonPropertiesResource.REST_PATH)
@Produces ("application/json")
@Consumes ("application/json")
public class AddonPropertiesResource
{
    public static final String VALUE_TOO_LONG_ERROR_MSG = String.format("The value cannot be bigger than %s.", FileUtils.byteCountToDisplaySize(AddonPropertyServiceImpl.MAXIMUM_PROPERTY_VALUE_LENGTH));
    public static final String REST_PATH = "addons/{addonKey}/properties";

    private static final Logger log = LoggerFactory.getLogger(AddonPropertiesResource.class);

    private final ApplicationProperties applicationProperties;
    private final AddonPropertyService addonPropertyService;
    private final AddonKeyExtractor addonKeyExtractor;
    private final I18nResolver i18nResolver;
    private final UserManager userManager;

    public AddonPropertiesResource(ApplicationProperties applicationProperties, AddonPropertyService addonPropertyService, AddonKeyExtractor addonKeyExtractor, I18nResolver i18nResolver, UserManager userManager)
    {
        this.applicationProperties = applicationProperties;
        this.addonPropertyService = addonPropertyService;
        this.addonKeyExtractor = addonKeyExtractor;
        this.i18nResolver = i18nResolver;
        this.userManager = userManager;
    }

    /**
     * Lists all properties of a plugin with the given plugin key.
     *
     * @param addonKey the add-on key of the plugin to fetch the property from
     * @param servletRequest the HTTP servlet request
     * @return a Response containing a list of properties or an error code with message.
     *
     * @response.representation.200.mediaType application/json
     * @response.representation.200.doc
     *      A list of property keys with links to themselves.
     * @response.representation.401.doc
     *      Request without credentials or with invalid credentials, e.g. by an uninstalled add-on.
     * @response.representation.404.doc
     *      Request issued by a user with insufficient credentials, e.g. for an add-on's data by anyone but the add-on itself, or for a plugin that does not exist.
     */
    @GET
    public Response getAddonProperties(@PathParam ("addonKey") final String addonKey, @Context HttpServletRequest servletRequest)
    {
        UserProfile user = userManager.getRemoteUser(servletRequest);
        String sourcePluginKey = addonKeyExtractor.getAddonKeyFromHttpRequest(servletRequest);

        return addonPropertyService.getAddonProperties(user, sourcePluginKey, addonKey).fold(
                new Function<AddonPropertyService.OperationStatus, Response>()
                {
                    @Override
                    public Response apply(final AddonPropertyService.OperationStatus status)
                    {
                        return getResponseBuilderFromOperationStatus(status).build();
                    }

                }, new Function<AddonPropertyIterable, Response>()
                {
                    @Override
                    public Response apply(final AddonPropertyIterable propertyIterable)
                    {
                        String baseURL = getRestPathForAddonKey(addonKey) + "/properties";
                        return Response.ok()
                                .entity(RestAddonPropertiesBean.valueOf(propertyIterable.getPropertyKeys(), baseURL))
                                .cacheControl(never())
                                .build();
                    }
                });
    }

    /**
     * Gets a property with the given property key.
     *
     * @param addonKey the add-on key of the plugin to fetch the property from
     * @param propertyKey the key of the property
     * @param returnJsonFormat set to true if the 'value' field should be returned in json format, false if you want string format. String format is deprecated.
     * @param servletRequest the HTTP servlet request
     * @return a Response containing a list of properties or an error code with message.
     *
     * @response.representation.200.mediaType application/json
     * @response.representation.200.doc
     *      The property containing key, value and link to self.
     * @response.representation.400.doc
     *      Property key longer than 127 characters.
     * @response.representation.401.doc
     *      Request without credentials or with invalid credentials, e.g. by an uninstalled add-on.
     * @response.representation.404.doc
     *      Request to get a property that does not exist.
     * @response.representation.404.doc
     *      Request issued by a user with insufficient credentials, e.g. for an add-on's data by anyone but the add-on itself, or for a plugin that does not exist.
     */
    @GET
    @Path ("{propertyKey}")
    public Response getAddonProperty(@PathParam ("addonKey") final String addonKey, @PathParam ("propertyKey") String propertyKey, @QueryParam("jsonValue") boolean returnJsonFormat, @Context final HttpServletRequest servletRequest)
    {
        UserProfile user = userManager.getRemoteUser(servletRequest);
        String sourcePluginKey = addonKeyExtractor.getAddonKeyFromHttpRequest(servletRequest);

        return addonPropertyService.getPropertyValue(user, sourcePluginKey, addonKey, propertyKey).fold(new Function<AddonPropertyService.OperationStatus, Response>()
        {
            @Override
            public Response apply(final AddonPropertyService.OperationStatus status)
            {
                return getResponseBuilderFromOperationStatus(status).build();
            }
        }, new Function<AddonProperty, Response>()
        {
            @Override
            public Response apply(final AddonProperty property)
            {
                String baseURL = getRestPathForAddonKey(addonKey) + "/properties";
                return Response.ok()
                        .entity(RestAddonProperty.valueOf(property, baseURL, returnJsonFormat))
                        .cacheControl(never())
                        .build();
            }
        });
    }
    /**
     * Creates or updates a property with the given property key.
     *
     * @param addonKey the add-on key of the plugin to fetch the property from
     * @param propertyKey the key of the property
     * @param request the HTTP request
     * @param servletRequest the HTTP servlet request
     * @return a Response containing a list of properties or an error code with message.
     *
     * @response.representation.200.mediaType application/json
     * @response.representation.200.doc
     *      Property has been updated.
     * @response.representation.201.mediaType application/json
     * @response.representation.201.doc
     *      Property has been created.
     * @response.representation.400.doc
     *      Property key longer than 127 characters.
     * @response.representation.401.doc
     *      Request without credentials or with invalid credentials, e.g. by an uninstalled add-on.
     * @response.representation.404.doc
     *      Request to get a property that does not exist.
     * @response.representation.404.doc
     *      Request issued by a user with insufficient credentials, e.g. for an add-on's data by anyone but the add-on itself, or for a plugin that does not exist.
     */
    @PUT
    @Path ("{propertyKey}")
    public Response putAddonProperty(@PathParam ("addonKey") final String addonKey, @PathParam ("propertyKey") final String propertyKey, @Context final Request request, @Context HttpServletRequest servletRequest)
    {
        final UserProfile user = userManager.getRemoteUser(servletRequest);
        // can be null, it is checked in the service.
        final String sourcePluginKey = addonKeyExtractor.getAddonKeyFromHttpRequest(servletRequest);

        Either<RestParamError, String> errorStringEither = propertyValue(servletRequest);
        return errorStringEither.fold(new Function<RestParamError, Response>()
        {
            @Override
            public Response apply(final RestParamError error)
            {
                return getResponseForMessageAndStatus(VALUE_TOO_LONG_ERROR_MSG, Response.Status.FORBIDDEN);
            }
        }, new Function<String, Response>()
        {
            @Override
            public Response apply(final String propertyValue)
            {
                return addonPropertyService.setPropertyValueIfConditionSatisfied(user, sourcePluginKey, addonKey, propertyKey, propertyValue, eTagValidationFunction(request))
                        .fold(onPreconditionFailed(), onFailure(), onSuccess());
            }
        });
    }

    /**
     * Deletes property with the given property key.
     *
     * @param addonKey the add-on key of the plugin to fetch the property from
     * @param propertyKey the key of the property
     * @param request the HTTP request
     * @param servletRequest the HTTP servlet request
     * @return a Response containing a list of properties or an error code with message.
     *
     * @response.representation.204.doc
     *      Nothing is returned on success.
     * @response.representation.400.doc
     *      Property key longer than 127 characters.
     * @response.representation.401.doc
     *      Request without credentials or with invalid credentials, e.g. by an uninstalled add-on.
     * @response.representation.404.doc
     *      Request to get a property that does not exist.
     * @response.representation.404.doc
     *      Request issued by a user with insufficient credentials, e.g. for an add-on's data by anyone but the add-on itself, or for a plugin that does not exist.
     */
    @DELETE
    @Path ("{propertyKey}")
    public Response deleteAddonProperty(@PathParam ("addonKey") final String addonKey, @PathParam ("propertyKey") final String propertyKey, @Context final Request request, @Context HttpServletRequest servletRequest)
    {
        final UserProfile user = userManager.getRemoteUser(servletRequest);
        // can be null, it is checked in the service.
        final String sourcePluginKey = addonKeyExtractor.getAddonKeyFromHttpRequest(servletRequest);

        return addonPropertyService.deletePropertyValueIfConditionSatisfied(user, sourcePluginKey, addonKey, propertyKey, eTagValidationFunction(request))
                .fold(onPreconditionFailed(), onFailure(), onDeleteSuccess());
    }

    private String getRestPathForAddonKey(final String key)
    {
        return applicationProperties.getBaseUrl(UrlMode.CANONICAL) + "/rest/atlassian-connect/1/addons" + "/" + key;
    }

    private Function<Optional<AddonProperty>, AddonPropertyService.ServiceConditionResult<Response.ResponseBuilder>> eTagValidationFunction(final Request request)
    {
        return new Function<Optional<AddonProperty>, AddonPropertyService.ServiceConditionResult<Response.ResponseBuilder>>()
        {
            @Override
            public AddonPropertyService.ServiceConditionResult<Response.ResponseBuilder> apply(final Optional<AddonProperty> propertyOption)
            {
                return AddonPropertyService.ServiceConditionResult.SUCCESS();
            }
        };
    }

    private Function<AddonPropertyService.OperationStatus, Response> onFailure()
    {
        return new Function<AddonPropertyService.OperationStatus, Response>()
        {
            @Override
            public Response apply(final AddonPropertyService.OperationStatus operationStatus)
            {
                return getResponseBuilderFromOperationStatus(operationStatus)
                        .build();
            }
        };
    }

    private Function<AddonPropertyService.PutOperationStatus, Response> onSuccess()
    {
        return new Function<AddonPropertyService.PutOperationStatus, Response>()
        {
            @Override
            public Response apply(final AddonPropertyService.PutOperationStatus operationStatus)
            {
                return getResponseBuilderFromOperationStatus(operationStatus)
                        .build();
            }
        };
    }

    private Function<Response.ResponseBuilder, Response> onPreconditionFailed()
    {
        return new Function<Response.ResponseBuilder, Response>()
        {
            @Override
            public Response apply(final Response.ResponseBuilder responseBuilder)
            {
                return responseBuilder.entity("").cacheControl(never()).build();
            }
        };
    }

    private Function<AddonPropertyService.OperationStatus, Response> onDeleteSuccess()
    {
        return new Function<AddonPropertyService.OperationStatus, Response>()
        {
            @Override
            public Response apply(final AddonPropertyService.OperationStatus operationStatus)
            {
                return getResponseBuilderFromOperationStatus(operationStatus).build();
            }
        };
    }

    private Response.ResponseBuilder getResponseBuilderFromOperationStatus(final AddonPropertyService.OperationStatus operationStatus)
    {
        return getResponseBuilderForMessageAndStatus(operationStatus.message(i18nResolver), Response.Status.fromStatusCode(operationStatus.getHttpStatusCode()));
    }

    private Response getResponseForMessageAndStatus(final String message, final Response.Status status)
    {
        return getResponseBuilderForMessageAndStatus(message, status).build();
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

    private Either<RestParamError, String> propertyValue(final HttpServletRequest request)
    {
        int contentLength = request.getContentLength();
        if (contentLength > AddonPropertyServiceImpl.MAXIMUM_PROPERTY_VALUE_LENGTH)
        {
            return Either.left(RestParamError.PROPERTY_VALUE_TOO_LONG);
        }

        try
        {
            char[] charData = IOUtils.toCharArray(request.getReader());
            return Either.right(new String(charData));
        }
        catch (IOException e)
        {
            return Either.left(RestParamError.PROPERTY_VALUE_TOO_LONG);
        }
    }

    private static CacheControl never()
    {
        CacheControl cacheNever = new CacheControl();
        cacheNever.setNoStore(true);
        cacheNever.setNoCache(true);

        return cacheNever;
    }

    private enum RestParamError
    {
        PROPERTY_VALUE_TOO_LONG
    }
}