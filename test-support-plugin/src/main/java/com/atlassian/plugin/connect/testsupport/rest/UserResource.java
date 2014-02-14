package com.atlassian.plugin.connect.testsupport.rest;

import com.atlassian.plugins.rest.common.security.AnonymousAllowed;
import com.atlassian.sal.api.user.UserManager;
import com.atlassian.sal.api.user.UserProfile;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import static java.lang.String.format;
import static javax.ws.rs.core.MediaType.*;
import static javax.ws.rs.core.Response.ok;

@Path("/")
public class UserResource
{
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final UserManager userManager;

    public UserResource(UserManager userManager)
    {
        this.userManager = userManager;
    }

    @GET
    @Produces(TEXT_PLAIN)
    @Path("/user")
    @AnonymousAllowed
    public Response getUserTextPlain()
    {
        final String content = getUsername();
        MediaType contentType = TEXT_PLAIN_TYPE;
        return getUser(content, contentType);
    }

    @GET
    @Produces(APPLICATION_JSON)
    @Path("/user")
    @AnonymousAllowed
    public Response getUserJson()
    {
        return getUser("{\"name\": \"%s\"}", APPLICATION_JSON_TYPE);
    }

    @GET
    @Produces(APPLICATION_XML)
    @Path("/user")
    @AnonymousAllowed
    public Response getUserXml()
    {
        return getUser("<user><name>%s</name></user>", APPLICATION_XML_TYPE);
    }

    private Response getUser(String format, MediaType contentType)
    {
        final String username = getUsername();
        logger.info("Getting the user '{}' as '{}'", username, contentType);
        return ok(format(format, username), contentType).build();
    }

    @GET
    @Produces(TEXT_PLAIN)
    @Path("/unscoped")
    public Response getUnScopedResource()
    {
        return buildErrorResponse();
    }

    @GET
    @Produces(TEXT_PLAIN)
    @Path("/unauthorisedscope")
    public Response getUnauthorisedScopeResource()
    {
        return buildErrorResponse();
    }

    private String getUsername()
    {
        UserProfile user = userManager.getRemoteUser();
        return user == null ? "anonymous" : user.getUsername();
    }

    private Response buildErrorResponse()
    {
        return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("This REST Resource should never be successfully called by a Remotable Plugin").build();
    }
}
