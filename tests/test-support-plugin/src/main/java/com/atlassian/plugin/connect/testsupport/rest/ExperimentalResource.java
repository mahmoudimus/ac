package com.atlassian.plugin.connect.testsupport.rest;

import com.atlassian.plugins.rest.common.security.AnonymousAllowed;
import com.atlassian.sal.api.user.UserManager;
import com.atlassian.sal.api.user.UserProfile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import static java.lang.String.format;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON_TYPE;
import static javax.ws.rs.core.Response.ok;

@Path("/experimental")
public class ExperimentalResource {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final UserManager userManager;

    public ExperimentalResource(UserManager userManager) {
        this.userManager = userManager;
    }

    @GET
    @Produces(APPLICATION_JSON)
    @Path("/user")
    @AnonymousAllowed
    public Response getUserJson(@HeaderParam("X-ExperimentalApi") String experimentalHeader) {
        if (experimentalHeader != null && experimentalHeader.equals("opt-in")) {
            return getUser("{\"name\": \"%s\"}", APPLICATION_JSON_TYPE);
        } else {
            return buildErrorResponse();
        }
    }

    private Response getUser(String format, MediaType contentType) {
        final String username = getUsername();
        logger.info("Getting the user '{}' as '{}'", username, contentType);
        return ok(format(format, username), contentType).build();
    }

    private String getUsername() {
        UserProfile user = userManager.getRemoteUser();
        return user == null ? "anonymous" : user.getUsername();
    }

    private Response buildErrorResponse() {
        return Response.status(Response.Status.PRECONDITION_FAILED).entity("Experimental header missing.").build();
    }
}
