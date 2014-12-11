package com.atlassian.plugin.connect.plugin.rest.addons;

import com.atlassian.plugin.connect.plugin.ao.AddOnProperty;
import com.atlassian.plugin.connect.plugin.installer.ConnectAddonManager;
import com.atlassian.plugin.connect.plugin.rest.RestError;
import com.atlassian.plugin.connect.plugin.rest.data.RestAddonProperty;
import com.atlassian.plugin.connect.plugin.service.AddOnPropertyService;
import com.atlassian.plugins.rest.common.security.AnonymousAllowed;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;

/**
 * TODO: Document this class / interface here
 *
 * @since v6.3
 */
@Path("addons2/{addonKey}/properties")
@Produces ("application/json")
@AnonymousAllowed
public class AddonsPropertyResource
{
    public static final String REST_PATH = "addons2/{addonKey}/properties";

    private final AddOnPropertyService addOnPropertyService;
    private final ConnectAddonManager connectAddonManager;

    public AddonsPropertyResource(final AddOnPropertyService addOnPropertyService, final ConnectAddonManager connectAddonManager)
    {
        this.addOnPropertyService = addOnPropertyService;
        this.connectAddonManager = connectAddonManager;
    }

    @GET
    public Response someMethod2(@PathParam("addonKey") String addonKey)
    {
        return Response.ok().build();
    }

    @GET
    @Path ("/{propertyKey}")
    public Response getAddonProperties(@PathParam ("addonKey") String addonKey, @PathParam("propertyKey") String propertyKey)
    {
        if (existsAddon(addonKey))
        {
            String message = "Add-on with key " + addonKey + " was not found";
            return getErrorResponse(message, Response.Status.NOT_FOUND);
        }

        AddOnPropertyService.ValidationResult<AddOnPropertyService.GetPropertyInput> validationResult = addOnPropertyService.validateGetPropertyValue(addonKey, propertyKey);
        if (!validationResult.isValid())
        {
//            ErrorCollection.Reason worstReason = ErrorCollection.Reason.getWorstReason(validationResult.getErrorCollection().getReasons());
//            return Response.status(worstReason.getHttpStatusCode()).entity(com.atlassian.jira.rest.api.util.ErrorCollection.of(validationResult.getErrorCollection())).cacheControl(never()).build();
        }
        AddOnProperty addonProperty = addOnPropertyService.getPropertyValue(validationResult);

        return Response.ok().entity(RestAddonProperty.valueOf(addonProperty)).build();

    }

    @PUT
    @Path ("/{key}")
    public Response putKey(@PathParam ("addonKey") String addonKey, @PathParam("key") String propertyKey)
    {
        if (existsAddon(addonKey))
        {
            String message = "Add-on with key " + addonKey + " was not found";
            return getErrorResponse(message, Response.Status.NOT_FOUND);
        }

//        addOnPropertyService.setPropertyValue(addonKey,propertyKey,propertyValue(request));
        return Response.ok().build();
    }

    private boolean existsAddon(final String addonKey)
    {
        return connectAddonManager.getExistingAddon(addonKey) != null;
    }

    private String propertyValue(final HttpServletRequest request)
    {
//        try
//        {
//            LimitInputStream limitInputStream =
//                    new LimitInputStream(request.getInputStream(), JsonEntityPropertyManagerImpl.MAXIMUM_VALUE_LENGTH + 1);
//            byte[] bytes = IOUtils.toByteArray(limitInputStream);
//            if (bytes.length > AddOnPropertyServiceImpl.MAXIMUM_VALUE_LENGTH)
//            {
//                return null;
//            }
//            return new String(bytes, Charset.defaultCharset());//forName(ComponentAccessor.getApplicationProperties().getEncoding()));
//        }
//        catch (IOException e)
//        {
//            return null;
//            throw new BadRequestWebException(ErrorCollection.of(e.getMessage()));
//        }
            return "";
    }

    private Response getErrorResponse(final String message, final Response.Status status)
    {
        RestError error = new RestError(status.getStatusCode(), message);
        return Response.status(status)
                .entity(error)
                .build();
    }
}
