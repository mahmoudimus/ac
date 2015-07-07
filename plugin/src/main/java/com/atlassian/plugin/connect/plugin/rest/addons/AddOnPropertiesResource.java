package com.atlassian.plugin.connect.plugin.rest.addons;

import com.atlassian.fugue.Either;
import com.atlassian.fugue.Option;
import com.atlassian.plugin.connect.api.scopes.AddOnKeyExtractor;
import com.atlassian.plugin.connect.plugin.property.AddOnProperty;
import com.atlassian.plugin.connect.plugin.property.AddOnPropertyIterable;
import com.atlassian.plugin.connect.plugin.property.AddOnPropertyService;
import com.atlassian.plugin.connect.plugin.property.AddOnPropertyServiceImpl;
import com.atlassian.plugin.connect.plugin.rest.RestResult;
import com.atlassian.plugin.connect.plugin.rest.data.RestAddOnPropertiesBean;
import com.atlassian.plugin.connect.plugin.rest.data.RestAddOnProperty;
import com.atlassian.sal.api.ApplicationProperties;
import com.atlassian.sal.api.UrlMode;
import com.atlassian.sal.api.message.I18nResolver;
import com.atlassian.sal.api.user.UserManager;
import com.atlassian.sal.api.user.UserProfile;
import com.google.common.base.Function;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.output.ByteArrayOutputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.CacheControl;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Request;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.nio.charset.Charset;

/**
 * REST endpoint for add-on properties
 */
@Path (AddOnPropertiesResource.REST_PATH)
@Produces ("application/json")
@Consumes ("application/json")
public class AddOnPropertiesResource
{
    public static final String VALUE_TOO_LONG_ERROR_MSG = String.format("The value cannot be bigger than %s.", FileUtils.byteCountToDisplaySize(AddOnPropertyServiceImpl.MAXIMUM_PROPERTY_VALUE_LENGTH));
    public static final String REST_PATH = "addons/{addonKey}/properties";

    private static final Logger log = LoggerFactory.getLogger(AddOnPropertiesResource.class);

    private final ApplicationProperties applicationProperties;
    private final AddOnPropertyService addOnPropertyService;
    private final AddOnKeyExtractor addOnKeyExtractor;
    private final I18nResolver i18nResolver;
    private final UserManager userManager;

    public AddOnPropertiesResource(ApplicationProperties applicationProperties, AddOnPropertyService addOnPropertyService, AddOnKeyExtractor addOnKeyExtractor, I18nResolver i18nResolver, UserManager userManager)
    {
        this.applicationProperties = applicationProperties;
        this.addOnPropertyService = addOnPropertyService;
        this.addOnKeyExtractor = addOnKeyExtractor;
        this.i18nResolver = i18nResolver;
        this.userManager = userManager;
    }

    /**
     * Lists all properties of a plugin with the given plugin key.
     *
     * @param addOnKey the add-on key of the plugin to fetch the property from
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
    public Response getAddOnProperties(@PathParam ("addonKey") final String addOnKey, @Context HttpServletRequest servletRequest)
    {
        UserProfile user = userManager.getRemoteUser(servletRequest);
        String sourcePluginKey = addOnKeyExtractor.getAddOnKeyFromHttpRequest(servletRequest);

        return addOnPropertyService.getAddOnProperties(user, sourcePluginKey, addOnKey).fold(
                new Function<AddOnPropertyService.OperationStatus, Response>()
                {
                    @Override
                    public Response apply(final AddOnPropertyService.OperationStatus status)
                    {
                        return getResponseBuilderFromOperationStatus(status).build();
                    }

                }, new Function<AddOnPropertyIterable, Response>()
                {
                    @Override
                    public Response apply(final AddOnPropertyIterable propertyIterable)
                    {
                        String baseURL = getRestPathForAddOnKey(addOnKey) + "/properties";
                        return Response.ok()
                                .entity(RestAddOnPropertiesBean.valueOf(propertyIterable.getPropertyKeys(), baseURL))
                                .cacheControl(never())
                                .build();
                    }
                });
    }

    /**
     * Gets a property with the given property key.
     *
     * @param addOnKey the add-on key of the plugin to fetch the property from
     * @param propertyKey the key of the property
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
    public Response getAddOnProperty(@PathParam ("addonKey") final String addOnKey, @PathParam ("propertyKey") String propertyKey, @Context final HttpServletRequest servletRequest)
    {
        UserProfile user = userManager.getRemoteUser(servletRequest);
        String sourcePluginKey = addOnKeyExtractor.getAddOnKeyFromHttpRequest(servletRequest);

        return addOnPropertyService.getPropertyValue(user, sourcePluginKey, addOnKey, propertyKey).fold(new Function<AddOnPropertyService.OperationStatus, Response>()
        {
            @Override
            public Response apply(final AddOnPropertyService.OperationStatus status)
            {
                return getResponseBuilderFromOperationStatus(status).build();
            }
        }, new Function<AddOnProperty, Response>()
        {
            @Override
            public Response apply(final AddOnProperty property)
            {
                String baseURL = getRestPathForAddOnKey(addOnKey) + "/properties";
                return Response.ok()
                        .entity(RestAddOnProperty.valueOf(property, baseURL))
                        .cacheControl(never())
                        .build();
            }
        });
    }
    /**
     * Creates or updates a property with the given property key.
     *
     * @param addOnKey the add-on key of the plugin to fetch the property from
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
    public Response putAddOnProperty(@PathParam ("addonKey") final String addOnKey, @PathParam ("propertyKey") final String propertyKey, @Context final Request request, @Context HttpServletRequest servletRequest)
    {
        final UserProfile user = userManager.getRemoteUser(servletRequest);
        // can be null, it is checked in the service.
        final String sourcePluginKey = addOnKeyExtractor.getAddOnKeyFromHttpRequest(servletRequest);

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
                return addOnPropertyService.setPropertyValueIfConditionSatisfied(user, sourcePluginKey, addOnKey, propertyKey, propertyValue, eTagValidationFunction(request))
                        .fold(onPreconditionFailed(), onFailure(), onSuccess());
            }
        });
    }

    /**
     * Deletes property with the given property key.
     *
     * @param addOnKey the add-on key of the plugin to fetch the property from
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
    public Response deleteAddOnProperty(@PathParam ("addonKey") final String addOnKey, @PathParam ("propertyKey") final String propertyKey, @Context final Request request, @Context HttpServletRequest servletRequest)
    {
        final UserProfile user = userManager.getRemoteUser(servletRequest);
        // can be null, it is checked in the service.
        final String sourcePluginKey = addOnKeyExtractor.getAddOnKeyFromHttpRequest(servletRequest);

        return addOnPropertyService.deletePropertyValueIfConditionSatisfied(user, sourcePluginKey, addOnKey, propertyKey, eTagValidationFunction(request))
                .fold(onPreconditionFailed(), onFailure(), onDeleteSuccess());
    }

    private String getRestPathForAddOnKey(final String key)
    {
        return applicationProperties.getBaseUrl(UrlMode.CANONICAL) + "/rest/atlassian-connect/1/addons" + "/" + key;
    }

    private Function<Option<AddOnProperty>, AddOnPropertyService.ServiceConditionResult<Response.ResponseBuilder>> eTagValidationFunction(final Request request)
    {
        return new Function<Option<AddOnProperty>, AddOnPropertyService.ServiceConditionResult<Response.ResponseBuilder>>()
        {
            @Override
            public AddOnPropertyService.ServiceConditionResult<Response.ResponseBuilder> apply(final Option<AddOnProperty> propertyOption)
            {
                return AddOnPropertyService.ServiceConditionResult.SUCCESS();
            }
        };
    }

    private Function<AddOnPropertyService.OperationStatus, Response> onFailure()
    {
        return new Function<AddOnPropertyService.OperationStatus, Response>()
        {
            @Override
            public Response apply(final AddOnPropertyService.OperationStatus operationStatus)
            {
                return getResponseBuilderFromOperationStatus(operationStatus)
                        .build();
            }
        };
    }

    private Function<AddOnPropertyService.PutOperationStatus, Response> onSuccess()
    {
        return new Function<AddOnPropertyService.PutOperationStatus, Response>()
        {
            @Override
            public Response apply(final AddOnPropertyService.PutOperationStatus operationStatus)
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

    private Function<AddOnPropertyService.OperationStatus, Response> onDeleteSuccess()
    {
        return new Function<AddOnPropertyService.OperationStatus, Response>()
        {
            @Override
            public Response apply(final AddOnPropertyService.OperationStatus operationStatus)
            {
                return getResponseBuilderFromOperationStatus(operationStatus).build();
            }
        };
    }

    private Response.ResponseBuilder getResponseBuilderFromOperationStatus(final AddOnPropertyService.OperationStatus operationStatus)
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
        if (contentLength > AddOnPropertyServiceImpl.MAXIMUM_PROPERTY_VALUE_LENGTH)
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
