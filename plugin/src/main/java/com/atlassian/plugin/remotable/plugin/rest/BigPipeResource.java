package com.atlassian.plugin.remotable.plugin.rest;

import com.atlassian.plugin.remotable.plugin.util.http.bigpipe.BigPipe;
import com.atlassian.plugins.rest.common.security.AnonymousAllowed;
import org.json.JSONArray;
import org.json.JSONException;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.CacheControl;
import javax.ws.rs.core.Response;
import java.io.IOException;

/**
 * A REST resource that returns content as it is available.
 */
@Path("/bigpipe")
public class BigPipeResource
{
    private final BigPipe bigPipe;

    public BigPipeResource(BigPipe bigPipe)
    {
        this.bigPipe = bigPipe;
    }

    /**
     * Returns available content in the form of of JSON map:
     * - id : The content id
     * - html : The html to insert into the content placeholder
     */
    @GET
    @Path("/request/{id}")
    @AnonymousAllowed
    @Produces("application/json")
    public Response getContent(@PathParam("id") String requestId) throws IOException,
            InterruptedException, JSONException
    {
        JSONArray result = bigPipe.convertContentHandlersToJson(bigPipe.waitForCompletedHandlers(requestId));
        CacheControl cacheControl = new CacheControl();
        cacheControl.setNoCache(true);
        return Response.ok(result.toString(2)).cacheControl(cacheControl).build();
    }
}
