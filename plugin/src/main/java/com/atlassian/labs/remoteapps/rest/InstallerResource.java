package com.atlassian.labs.remoteapps.rest;

import com.atlassian.labs.remoteapps.DescriptorValidator;
import com.atlassian.labs.remoteapps.PermissionDeniedException;
import com.atlassian.labs.remoteapps.installer.RemoteAppInstaller;
import com.atlassian.plugins.rest.common.security.AnonymousAllowed;
import com.atlassian.sal.api.user.UserManager;
import com.sun.research.ws.wadl.Param;

import javax.ws.rs.*;
import javax.ws.rs.core.Response;
import java.net.URI;
import java.net.URISyntaxException;

/**
 *
 */
@Path("/installer")
public class InstallerResource
{
    private final RemoteAppInstaller remoteAppInstaller;
    private final UserManager userManager;
    private final DescriptorValidator descriptorValidator;

    public InstallerResource(RemoteAppInstaller remoteAppInstaller,
                             UserManager userManager,
                             DescriptorValidator descriptorValidator
    )
    {
        this.remoteAppInstaller = remoteAppInstaller;
        this.userManager = userManager;
        this.descriptorValidator = descriptorValidator;
    }

    @POST
    public Response install(@FormParam("url") String registrationUrl, @FormParam("token")
                            String registrationToken)
    {
        String token = registrationToken != null ? registrationToken : "";
        try
        {
            new URI(registrationUrl);
        }
        catch (URISyntaxException e)
        {
            return Response.status(Response.Status.BAD_REQUEST).entity("Invalid URI: '" + registrationUrl + "'").build();
        }
        try
        {
            remoteAppInstaller.install(userManager.getRemoteUsername(), registrationUrl, token);
        }
        catch (PermissionDeniedException ex)
        {
            return Response.status(Response.Status.FORBIDDEN).entity(ex.getMessage()).build();
        }
        catch (RuntimeException ex)
        {
            ex.printStackTrace();
            return Response.status(Response.Status.BAD_REQUEST).entity(ex.getMessage()).build();
        }
        return Response.ok().build();
    }

    @GET
    @Path("/schema/remote-app")
    @Produces("text/xml")
    @AnonymousAllowed
    public Response getSchema()
    {
        return Response.ok().entity(descriptorValidator.getSchema()).build();
    }
}
