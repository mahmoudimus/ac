package com.atlassian.plugin.remotable.plugin.product.confluence.rest;

import com.atlassian.plugin.remotable.plugin.module.confluence.MacroContentManager;
import com.atlassian.plugin.remotable.plugin.module.permission.ApiScopingFilter;
import com.atlassian.plugins.rest.common.security.AnonymousAllowed;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.DELETE;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Context;
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
    @AnonymousAllowed
    @DELETE
    public Response clearMacrosFromPluginKey(@Context HttpServletRequest request, @PathParam("appKey") String appKey)
    {
        String consumerKey = (String) request.getAttribute(ApiScopingFilter.PLUGIN_KEY);
        if (consumerKey == null || !consumerKey.equals(appKey))
        {
            return Response.status(Response.Status.FORBIDDEN).build();
        }
        macroContentManager.clearContentByPluginKey(appKey);
        return Response.noContent().build();
    }

    @Path("/app/{appKey}/{key}")
    @AnonymousAllowed
    @DELETE
    public Response clearMacro(@Context HttpServletRequest request, @PathParam("appKey") String appKey, @PathParam("key") String macroInstanceKey)
    {
        String consumerKey = (String) request.getAttribute(ApiScopingFilter.PLUGIN_KEY);
        if (consumerKey == null || !consumerKey.equals(appKey))
        {
            return Response.status(Response.Status.FORBIDDEN).build();
        }
        macroContentManager.clearContentByInstance(appKey, macroInstanceKey);
        return Response.noContent().build();
    }
}
