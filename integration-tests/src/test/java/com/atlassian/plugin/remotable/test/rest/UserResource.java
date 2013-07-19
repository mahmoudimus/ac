package com.atlassian.plugin.remotable.test.rest;

import com.atlassian.plugins.rest.common.security.AnonymousAllowed;
import com.atlassian.sal.api.user.UserManager;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Path("/")
public class UserResource
{
    private final UserManager userManager;

    public UserResource(UserManager userManager)
    {
        this.userManager = userManager;
    }

    @GET
    @Produces(MediaType.TEXT_PLAIN)
    @Path("/user")
    @AnonymousAllowed
    public Response getUser()
    {
        return Response.ok(userManager.getRemoteUsername()).build();
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/user")
    @AnonymousAllowed
    public Response getUserJson()
    {
        String username = userManager.getRemoteUsername();
        return Response.ok("{\"name\": \"" + username + "\"}", MediaType.APPLICATION_JSON_TYPE).build();
    }

    @GET
    @Produces(MediaType.APPLICATION_XML)
    @Path("/user")
    @AnonymousAllowed
    public Response getUserXml()
    {
        String username = userManager.getRemoteUsername();
        return Response.ok("<user><name>" + username + "</name></user>", MediaType.APPLICATION_XML_TYPE).build();
    }

    @GET
    @Produces(MediaType.TEXT_PLAIN)
    @Path("/unscoped")
    public Response getUnScopedResource()
    {
        return buildErrorResponse();
    }

    @GET
    @Produces(MediaType.TEXT_PLAIN)
    @Path("/unauthorisedscope")
    public Response getUnauthorisedScopeResource()
    {
        return buildErrorResponse();
    }
    
    private Response buildErrorResponse()
    {
        return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("This REST Resource should never be successfully called by a Remotable Plugin").build();
    }
}
