package com.atlassian.plugin.connect.plugin.rest;

import com.atlassian.plugin.connect.plugin.descriptor.DescriptorValidator;
import com.atlassian.plugins.rest.common.security.AnonymousAllowed;
import org.bouncycastle.openssl.PEMWriter;
import org.json.JSONException;
import org.json.JSONObject;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.io.StringWriter;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;

import static com.atlassian.plugin.connect.plugin.rest.InstallerResource.INSTALLER_RESOURCE_PATH;

@Path(INSTALLER_RESOURCE_PATH)
public class InstallerResource
{
    public static final String INSTALLER_RESOURCE_PATH = "/installer";
    public static final String ATLASSIAN_PLUGIN_REMOTABLE_SCHEMA_PATH = "/schema/atlassian-plugin-remotable";

    private final DescriptorValidator descriptorValidator;

    public InstallerResource(DescriptorValidator descriptorValidator)
    {
        this.descriptorValidator = descriptorValidator;
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
