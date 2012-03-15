package com.atlassian.labs.remoteapps.rest;

import com.atlassian.labs.remoteapps.DescriptorValidator;
import com.atlassian.labs.remoteapps.api.InstallationFailedException;
import com.atlassian.labs.remoteapps.api.PermissionDeniedException;
import com.atlassian.labs.remoteapps.api.RemoteAppsService;
import com.atlassian.labs.remoteapps.settings.SettingsManager;
import com.atlassian.plugins.rest.common.security.AnonymousAllowed;
import com.atlassian.sal.api.user.UserManager;
import org.bouncycastle.openssl.PEMWriter;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.DELETE;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.io.StringWriter;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;

/**
 *
 */
@Path("/installer")
public class InstallerResource
{
    private static final Logger log = LoggerFactory.getLogger(InstallerResource.class);
    private final RemoteAppsService remoteAppsService;
    private final UserManager userManager;
    private final DescriptorValidator descriptorValidator;
    private final SettingsManager settingsManager;

    public InstallerResource(UserManager userManager,
                             DescriptorValidator descriptorValidator,
                             SettingsManager settingsManager, RemoteAppsService remoteAppsService)
    {
        this.remoteAppsService = remoteAppsService;
        this.userManager = userManager;
        this.descriptorValidator = descriptorValidator;
        this.settingsManager = settingsManager;
    }

    @PUT
    @Path("/allow-dogfooding")
    public Response allowDogfooding()
    {
        if (!userManager.isAdmin(userManager.getRemoteUsername()))
        {
            return Response.status(Response.Status.FORBIDDEN).build();
        }
        settingsManager.setAllowDogfooding(true);
        return Response.noContent().build();
    }

    @DELETE
    @Path("/allow-dogfooding")
    public Response disallowDogfooding()
    {
        if (!userManager.isAdmin(userManager.getRemoteUsername()))
        {
            return Response.status(Response.Status.FORBIDDEN).build();
        }
        settingsManager.setAllowDogfooding(false);
        return Response.noContent().build();
    }
    
    @GET
    @Path("/allow-dogfooding")
    public Response doesAllowDogfooding()
    {
        if (settingsManager.isAllowDogfooding())
        {
            return Response.noContent().build();
        }
        else
        {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
    }

    @POST
    public Response install(@FormParam("url") String registrationUrl, @FormParam("token")
                            String registrationToken)
    {
        String token = registrationToken != null ? registrationToken : "";

        try
        {
            remoteAppsService.install(userManager.getRemoteUsername(), registrationUrl, token);
        }
        catch (PermissionDeniedException ex)
        {
            return Response.status(Response.Status.FORBIDDEN).entity(ex.getMessage()).build();
        }
        catch (InstallationFailedException ex)
        {
            log.debug(ex.getMessage(), ex);
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

    @POST
    @Path("/keygen")
    @Produces("application/json")
    public Response generateKeys() throws NoSuchAlgorithmException, IOException, JSONException
    {
        KeyPairGenerator gen = KeyPairGenerator.getInstance("RSA");
        KeyPair pair = gen.generateKeyPair();
        StringWriter publicKeyWriter = new StringWriter();
        PEMWriter pubWriter = new PEMWriter(publicKeyWriter);
        pubWriter.writeObject(pair.getPublic());
        pubWriter.close();

        StringWriter privateKeyWriter = new StringWriter();
        PEMWriter privWriter = new PEMWriter(privateKeyWriter);
        privWriter.writeObject(pair.getPrivate());
        privWriter.close();

        return Response.ok(new JSONObject()
                .put("publicKey", publicKeyWriter.toString())
                .put("privateKey", privateKeyWriter.toString()).toString(2))
                .build();

    }
}
