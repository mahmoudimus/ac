package com.atlassian.plugin.connect.util.fixture.rest;

import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 * This class provides mock resources for testing JIRA Agile Scopes.
 */
@Path ("/")
@Produces (MediaType.APPLICATION_JSON)
public class MockAgileResources {

    @Path ("rapidview")
    @GET
    public Response rapidview()
    {
        return Response.ok().build();
    }

    @Path ("api/rank/before")
    @PUT
    public Response rankBefore()
    {
        return Response.ok().build();
    }



}
