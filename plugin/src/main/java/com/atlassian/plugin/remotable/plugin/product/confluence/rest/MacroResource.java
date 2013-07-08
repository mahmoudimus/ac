package com.atlassian.plugin.remotable.plugin.product.confluence.rest;

import com.atlassian.fugue.Option;
import com.atlassian.plugin.remotable.plugin.module.confluence.MacroContentManager;
import com.atlassian.plugin.remotable.plugin.module.permission.ApiScopingFilter;
import com.atlassian.plugins.rest.common.security.AnonymousAllowed;
import com.google.common.base.Function;
import com.google.common.base.Suppliers;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.DELETE;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;

import static com.atlassian.fugue.Option.option;

@Path("/macro")
@AnonymousAllowed
public class MacroResource
{
    private final MacroContentManager macroContentManager;

    public MacroResource(MacroContentManager macroContentManager)
    {
        this.macroContentManager = macroContentManager;
    }

    @Path("/app/{appKey}")
    @DELETE
    public Response clearMacrosFromPluginKey(@Context HttpServletRequest request, @PathParam("appKey") String appKey)
    {
        if (isAuthorisedConsumer(request, appKey))
        {
            macroContentManager.clearContentByPluginKey(appKey);
            return Response.noContent().build();
        }
        else
        {
            return Response.status(Response.Status.FORBIDDEN).build();
        }
    }

    @Path("/app/{appKey}/{key}")
    @DELETE
    public Response clearMacro(@Context HttpServletRequest request, @PathParam("appKey") String appKey, @PathParam("key") String macroInstanceKey)
    {
        if (isAuthorisedConsumer(request, appKey))
        {
            macroContentManager.clearContentByInstance(appKey, macroInstanceKey);
            return Response.noContent().build();
        }
        else
        {
            return Response.status(Response.Status.FORBIDDEN).build();
        }
    }

    private boolean isAuthorisedConsumer(HttpServletRequest request, final String appKey)
    {
        return getConsumerKeyFromRequest(request).fold(
                Suppliers.ofInstance(Boolean.FALSE),
                new Function<String, Boolean>()
                {
                    @Override
                    public Boolean apply(String key)
                    {
                        return key.equals(appKey);
                    }
                });
    }

    private Option<String> getConsumerKeyFromRequest(HttpServletRequest request)
    {
        return option((String) request.getAttribute(ApiScopingFilter.PLUGIN_KEY));
    }
}
