package com.atlassian.plugin.connect.plugin.rest.addons;

import com.atlassian.plugin.connect.plugin.installer.ConnectAddonManager;
import com.atlassian.plugin.connect.plugin.rest.RestError;
import com.atlassian.plugin.connect.plugin.service.AddOnPropertyService;
import com.atlassian.plugins.rest.common.security.AnonymousAllowed;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;

/**
 * TODO: Document this class / interface here
 *
 * @since v6.3
 */
@Path("addons/{addonKey}/properties")
@Produces ("application/json")
@AnonymousAllowed
public class AddonsPropertyResource
{
    public static final String REST_PATH = "addons/{addonKey}/properties";

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


    private boolean existsAddon(final String addonKey)
    {
        return connectAddonManager.getExistingAddon(addonKey) != null;
    }


    private Response getErrorResponse(final String message, final Response.Status status)
    {
        RestError error = new RestError(status.getStatusCode(), message);
        return Response.status(status)
                .entity(error)
                .build();
    }
}
