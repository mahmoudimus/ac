package com.atlassian.labs.remoteapps.product.confluence;

import com.atlassian.confluence.macro.Macro;
import com.atlassian.confluence.plugin.descriptor.XhtmlMacroModuleDescriptor;
import com.atlassian.labs.remoteapps.modules.confluence.ImagePlaceholderMacroWrapper;
import com.atlassian.plugin.PluginAccessor;
import com.google.common.collect.Maps;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.core.UriBuilder;
import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class RemoteAppsImagePlaceholderServlet extends HttpServlet
{
    private final PluginAccessor pluginAccessor;

    public RemoteAppsImagePlaceholderServlet(PluginAccessor pluginAccessor)
    {
        this.pluginAccessor = pluginAccessor;
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException
    {
        Map<String, String[]> paramList = Maps.newHashMap(req.getParameterMap());
        List<Pair> params = new ArrayList<Pair>();
        for (Map.Entry<String, String[]> entry : paramList.entrySet())
        {
            for (String value : entry.getValue())
            {
                params.add(new Pair(entry.getKey(), value));
            }
        }

        String pluginKey = req.getParameter("pluginKey");
        String macroKey = req.getParameter("macroKey");

        if (pluginKey == null || macroKey == null)
        {
            resp.sendError(404);
            return;
        }

        XhtmlMacroModuleDescriptor xhtmlMacroModuleDescriptor = (XhtmlMacroModuleDescriptor) pluginAccessor.getEnabledPluginModule(pluginKey + ":" + macroKey);
        Macro macro = xhtmlMacroModuleDescriptor.getModule();
        if (!(macro instanceof ImagePlaceholderMacroWrapper))
        {
            resp.sendError(500);
            return;
        }

        ImagePlaceholderMacroWrapper remoteMacro = (ImagePlaceholderMacroWrapper) macro;
        UriBuilder builder = UriBuilder.fromUri(remoteMacro.getBaseUrl());
        builder.path(remoteMacro.getImageUrl());
        for (Pair p : params)
        {
            builder.queryParam(p.key, p.value);
        }
        URI imageUri = builder.build();
        resp.sendRedirect(imageUri.toString());
    }

    private static class Pair
    {
        String key;
        String value;

        private Pair(String key, String value)
        {
            this.key = key;
            this.value = value;
        }
    }
}
