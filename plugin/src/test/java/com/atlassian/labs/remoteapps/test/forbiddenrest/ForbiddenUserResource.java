package com.atlassian.labs.remoteapps.test.forbiddenrest;

import com.atlassian.labs.remoteapps.test.rest.UserResource;
import com.atlassian.sal.api.user.UserManager;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;

/**
 *
 */
@Path("/")
public class ForbiddenUserResource extends UserResource
{
    public ForbiddenUserResource(UserManager userManager)
    {
        super(userManager);
    }
}
