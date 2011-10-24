package com.atlassian.labs.remoteapps.rest;

import com.atlassian.labs.remoteapps.installer.RemoteAppInstaller;
import com.sun.research.ws.wadl.Param;

import javax.ws.rs.FormParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
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

    public InstallerResource(RemoteAppInstaller remoteAppInstaller)
    {
        this.remoteAppInstaller = remoteAppInstaller;
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
            return Response.status(Response.Status.BAD_REQUEST).build();
        }
        try
        {
            remoteAppInstaller.install(registrationUrl, token);
        }
        catch (RuntimeException ex)
        {
            ex.printStackTrace();
            return Response.status(Response.Status.BAD_REQUEST).build();
        }
        return Response.ok().build();
    }
}
