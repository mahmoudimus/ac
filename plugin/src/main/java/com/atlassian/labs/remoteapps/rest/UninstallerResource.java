package com.atlassian.labs.remoteapps.rest;

import com.atlassian.labs.remoteapps.DescriptorValidator;
import com.atlassian.labs.remoteapps.PermissionDeniedException;
import com.atlassian.labs.remoteapps.RemoteAppsService;
import com.atlassian.labs.remoteapps.installer.RemoteAppInstaller;
import com.atlassian.labs.remoteapps.settings.SettingsManager;
import com.atlassian.plugins.rest.common.security.AnonymousAllowed;
import com.atlassian.sal.api.user.UserManager;
import org.bouncycastle.openssl.PEMWriter;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.*;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.io.StringWriter;
import java.net.URI;
import java.net.URISyntaxException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;

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
