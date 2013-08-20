package com.atlassian.plugin.connect.plugin.rest;

import java.io.IOException;
import java.io.StringWriter;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;

import javax.ws.rs.*;
import javax.ws.rs.core.Response;

import com.atlassian.plugin.connect.plugin.descriptor.DescriptorValidator;
import com.atlassian.plugin.connect.plugin.settings.SettingsManager;
import com.atlassian.plugins.rest.common.security.AnonymousAllowed;
import com.atlassian.sal.api.user.UserManager;

import org.bouncycastle.openssl.PEMWriter;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.atlassian.plugin.connect.plugin.rest.InstallerResource.INSTALLER_RESOURCE_PATH;

@Path(INSTALLER_RESOURCE_PATH)
public class InstallerResource
{
    public static final String INSTALLER_RESOURCE_PATH = "/installer";
    public static final String ATLASSIAN_PLUGIN_REMOTABLE_SCHEMA_PATH = "/schema/atlassian-plugin-remotable";
    public static final String ATLASSIAN_PLUGIN_SCHEMA_PATH = "/schema/atlassian-plugin";

    private static final Logger log = LoggerFactory.getLogger(InstallerResource.class);

    private final UserManager userManager;
    private final DescriptorValidator descriptorValidator;
    private final SettingsManager settingsManager;

    public InstallerResource(UserManager userManager,
                             DescriptorValidator descriptorValidator,
                             SettingsManager settingsManager
    )
    {
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

    @GET
    @Path(ATLASSIAN_PLUGIN_REMOTABLE_SCHEMA_PATH)
    @Produces("text/xml")
    @AnonymousAllowed
    public Response getRemotePluginSchema()
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
