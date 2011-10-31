package com.atlassian.labs.remoteapps.test.rest;

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
    public Response getUser()
    {
        return Response.ok(userManager.getRemoteUsername()).build();
    }
}
