package com.atlassian.plugin.remotable.plugin.rest;

import com.atlassian.plugin.remotable.spi.PermissionDeniedException;
import com.atlassian.plugin.remotable.spi.RemotablePluginInstallationService;
import com.atlassian.sal.api.user.UserManager;

import javax.ws.rs.*;
import javax.ws.rs.core.Response;

/**
 *
 */
@Path("/uninstaller")
public class UninstallerResource
{
    private final RemotablePluginInstallationService remotablePluginInstallationService;
    private final UserManager userManager;

    public UninstallerResource(UserManager userManager, RemotablePluginInstallationService remotablePluginInstallationService)
    {
        this.remotablePluginInstallationService = remotablePluginInstallationService;
        this.userManager = userManager;
    }

    @DELETE
    @Path("/{appKey}")
    public Response uninstall(@PathParam("appKey") String appKey)
    {
        try
        {
            remotablePluginInstallationService.uninstall(userManager.getRemoteUsername(), appKey);
        }
        catch (PermissionDeniedException ex)
        {
            return Response.status(Response.Status.FORBIDDEN).entity(ex.getMessage()).build();
        }

        return Response.noContent().build();
    }
}
