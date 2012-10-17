package com.atlassian.plugin.remotable.plugin.rest;

import com.atlassian.plugin.remotable.plugin.descriptor.DescriptorValidator;
import com.atlassian.plugin.remotable.spi.InstallationFailedException;
import com.atlassian.plugin.remotable.spi.PermissionDeniedException;
import com.atlassian.plugin.remotable.spi.RemotablePluginInstallationService;
import com.atlassian.plugin.remotable.plugin.settings.SettingsManager;
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
import java.util.Set;

/**
 *
 */
@Path("/installer")
public class InstallerResource
{
    private static final Logger log = LoggerFactory.getLogger(InstallerResource.class);
    private final RemotablePluginInstallationService remotablePluginInstallationService;
    private final UserManager userManager;
    private final DescriptorValidator descriptorValidator;
    private final SettingsManager settingsManager;

    public InstallerResource(UserManager userManager,
            DescriptorValidator descriptorValidator,
            SettingsManager settingsManager,
            RemotablePluginInstallationService remotablePluginInstallationService
    )
    {
        this.remotablePluginInstallationService = remotablePluginInstallationService;
        this.userManager = userManager;
        this.descriptorValidator = descriptorValidator;
        this.settingsManager = settingsManager;
    }

    @PUT
    @Path("/allow-dogfooding")
    public Response allowDogfooding()
    {
        if (!userManager.isSystemAdmin(userManager.getRemoteUsername()))
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
        if (!userManager.isSystemAdmin(userManager.getRemoteUsername()))
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
    public Response install(@FormParam("url") String registrationUrl,
            @FormParam("token") String registrationToken)
    {
        String token = registrationToken != null ? registrationToken : "";

        try
        {
            remotablePluginInstallationService.install(userManager.getRemoteUsername(),
                    registrationUrl, token);
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

    @POST
    @Path("/reinstall")
    @Produces("text/plain")
    public Response reinstall()
    {
        Set<String> keys = null;
        try
        {
            keys = remotablePluginInstallationService.reinstallRemotePlugins(
                    userManager.getRemoteUsername());
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
        return Response.ok().entity(keys.toString()).build();
    }

    @GET
    @Path("/schema/atlassian-plugin")
    @Produces("text/xml")
    @AnonymousAllowed
    public Response getPluginSchema()
    {
        return Response.ok().entity(descriptorValidator.getPluginSchema()).build();
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
