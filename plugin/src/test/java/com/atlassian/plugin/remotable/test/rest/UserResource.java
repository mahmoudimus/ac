package com.atlassian.plugin.remotable.test.rest;

import com.atlassian.sal.api.user.UserManager;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;

/**
 *
 */
@Path("/")
public class UserResource
{
    private final UserManager userManager;

    public UserResource(UserManager userManager)
    {
        this.userManager = userManager;
    }

    @GET
    @Produces("text/plain")
    @Path("/user")
    public Response getUser()
    {
        return Response.ok(userManager.getRemoteUsername()).build();
    }

    @GET
    @Produces("text/plain")
    @Path("/unscoped")
    public Response getUnScopedResource()
    {
        return buildErrorResponse();
    }

    @GET
    @Path("/unauthorisedscope")
    @Produces("text/plain")
    public Response getUnauthorisedScopeResource()
    {
        return buildErrorResponse();
    }
    
    private Response buildErrorResponse()
    {
        return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("This REST Resource should never be successfully called by a Remotable Plugin").build();
    }
}
