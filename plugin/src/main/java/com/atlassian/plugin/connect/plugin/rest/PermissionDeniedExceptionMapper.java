package com.atlassian.plugin.connect.plugin.rest;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

import com.atlassian.plugin.connect.spi.PermissionDeniedException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Ensures permission denied exceptions come back in a nice for for rest
 */
@Provider
public class PermissionDeniedExceptionMapper implements ExceptionMapper<PermissionDeniedException>
{
    private static final Logger log = LoggerFactory.getLogger(PermissionDeniedExceptionMapper.class);

    public Response toResponse(PermissionDeniedException exception)
    {
        log.error(exception.getMessage(), exception.getCause());
        return Response.status(Response.Status.FORBIDDEN).
                entity(exception).
                type("application/json").
                build();
    }
}