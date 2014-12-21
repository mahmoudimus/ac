package com.atlassian.plugin.connect.plugin.rest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

/**
 * An exception mapper that instructs clients to authenticate using a JWT signed with their Atlassian Connect
 * add-on credentials.
 */
@Provider
public class ConnectAddonAuthenticationRequiredExceptionMapper
        implements ExceptionMapper<ConnectAddonAuthenticationRequiredException>
{

    public static final String AUTHENTICATION_REALM = "Atlassian Connect";

    private static final Logger log = LoggerFactory.getLogger(ConnectAddonAuthenticationRequiredExceptionMapper.class);

    @Override
    public Response toResponse(ConnectAddonAuthenticationRequiredException exception)
    {
        log.error(exception.getMessage(), exception.getCause());
        return Response.status(Response.Status.UNAUTHORIZED)
                .header(HttpHeaders.WWW_AUTHENTICATE, "JWT realm=\"" + AUTHENTICATION_REALM + "\"")
                .entity(exception)
                .build();
    }
}
