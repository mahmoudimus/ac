package com.atlassian.plugin.connect.confluence.macro.rest;

import java.util.Optional;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.DELETE;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;

import com.atlassian.plugin.connect.api.auth.scope.AddOnKeyExtractor;
import com.atlassian.plugin.connect.confluence.macro.MacroContentManager;
import com.atlassian.plugins.rest.common.security.AnonymousAllowed;

@Path("/macro")
@AnonymousAllowed
public class MacroResource
{
    private final MacroContentManager macroContentManager;
    private final AddOnKeyExtractor addOnKeyExtractor;

    public MacroResource(MacroContentManager macroContentManager, final AddOnKeyExtractor addOnKeyExtractor)
    {
        this.macroContentManager = macroContentManager;
        this.addOnKeyExtractor = addOnKeyExtractor;
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
        final Optional<String> potentialConsumerKeyFromRequest = getConsumerKeyFromRequest(request);
        return potentialConsumerKeyFromRequest.isPresent() && appKey.equals(potentialConsumerKeyFromRequest.get());
    }

    private Optional<String> getConsumerKeyFromRequest(HttpServletRequest request)
    {
        return Optional.ofNullable(addOnKeyExtractor.extractClientKey(request));
    }
}
