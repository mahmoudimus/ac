package com.atlassian.labs.remoteapps.rest;

import com.atlassian.labs.remoteapps.api.PermissionDeniedException;
import com.atlassian.labs.remoteapps.api.RemoteAppsService;
import com.atlassian.sal.api.user.UserManager;

import javax.ws.rs.*;
import javax.ws.rs.core.Response;

/**
 *
 */
@Path("/uninstaller")
public class UninstallerResource
{
    private final RemoteAppsService remoteAppsService;
    private final UserManager userManager;

    public UninstallerResource(UserManager userManager, RemoteAppsService remoteAppsService)
    {
        this.remoteAppsService = remoteAppsService;
        this.userManager = userManager;
    }

    @DELETE
    @Path("/{appKey}")
    public Response uninstall(@PathParam("appKey") String appKey)
    {
        try
        {
            remoteAppsService.uninstall(userManager.getRemoteUsername(), appKey);
        }
        catch (PermissionDeniedException ex)
        {
            return Response.status(Response.Status.FORBIDDEN).entity(ex.getMessage()).build();
        }

        return Response.noContent().build();
    }
}
