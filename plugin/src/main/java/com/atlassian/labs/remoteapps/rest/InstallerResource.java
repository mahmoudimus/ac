package com.atlassian.labs.remoteapps.rest;

import com.atlassian.labs.remoteapps.DescriptorValidator;
import com.atlassian.labs.remoteapps.PermissionDeniedException;
import com.atlassian.labs.remoteapps.installer.RemoteAppInstaller;
import com.atlassian.plugins.rest.common.security.AnonymousAllowed;
import com.atlassian.sal.api.user.UserManager;
import org.bouncycastle.openssl.PEMWriter;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
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
@Path("/installer")
public class InstallerResource
{
    private static final Logger log = LoggerFactory.getLogger(InstallerResource.class);
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
            log.error("Unable to install extension: " + ex.getMessage(), ex);
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
