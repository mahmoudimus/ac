package com.atlassian.labs.remoteapps.product.confluence.rest;

import com.atlassian.labs.remoteapps.modules.confluence.MacroContentManager;

import javax.ws.rs.DELETE;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Response;

/**
 *
 */
@Path("/macro")
public class MacroResource
{
    private final MacroContentManager macroContentManager;

    public MacroResource(MacroContentManager macroContentManager)
    {
        this.macroContentManager = macroContentManager;
    }

    @Path("/app/{appKey}")
    @DELETE
    public Response clearMacrosFromPluginKey(@PathParam("appKey") String appKey)
    {
        macroContentManager.clearContentByPluginKey(appKey);
        return Response.noContent().build();
    }

    @Path("/page/{pageId}")
    @DELETE
    public Response clearMacrosFromPage(@PathParam("pageId") long pageId)
    {
        macroContentManager.clearContentByPageId(pageId);
        return Response.noContent().build();
    }

    @Path("/page/{pageId}/{key}")
    @DELETE
    public Response clearMacrosByKey(@PathParam("pageId") long pageId, @PathParam("key") String key)
    {
        macroContentManager.clearContentByKey(pageId, key);
        return Response.noContent().build();
    }

}
