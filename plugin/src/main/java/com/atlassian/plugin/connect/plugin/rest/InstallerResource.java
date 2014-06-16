package com.atlassian.plugin.connect.plugin.rest;

import com.atlassian.plugin.connect.api.xmldescriptor.XmlDescriptor;
import com.atlassian.plugin.connect.plugin.descriptor.DescriptorValidator;
import com.atlassian.plugins.rest.common.security.AnonymousAllowed;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;

import static com.atlassian.plugin.connect.plugin.rest.InstallerResource.INSTALLER_RESOURCE_PATH;

@XmlDescriptor
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
}
