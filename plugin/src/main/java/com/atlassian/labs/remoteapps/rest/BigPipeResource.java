package com.atlassian.labs.remoteapps.rest;

import com.atlassian.labs.remoteapps.util.http.bigpipe.BigPipe;
import com.atlassian.labs.remoteapps.util.http.bigpipe.BigPipeHttpContentHandler;
import com.atlassian.plugins.rest.common.security.AnonymousAllowed;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.CacheControl;
import javax.ws.rs.core.Context;
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
        Iterable<BigPipeHttpContentHandler> handlers = bigPipe.consumeCompletedHandlers(requestId);
        JSONArray result = new JSONArray();
        for (BigPipeHttpContentHandler handler : handlers)
        {
            String html = handler.getFinalContent();
            JSONObject content = new JSONObject();
            content.put("id", handler.getContentId());
            content.put("html", html);
            result.put(content);
        }
        CacheControl cacheControl = new CacheControl();
        cacheControl.setNoCache(true);
        return Response.ok(result.toString(2)).cacheControl(cacheControl).build();
    }
}
