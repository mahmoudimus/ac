package com.atlassian.labs.remoteapps.test;

import com.atlassian.plugin.ModuleDescriptor;
import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.osgi.bridge.external.PluginRetrievalService;
import com.atlassian.plugin.servlet.descriptors.ServletModuleDescriptor;
import com.atlassian.templaterenderer.TemplateRenderer;
import com.google.common.collect.ImmutableMap;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;

import static com.google.common.collect.Maps.newHashMap;
import static java.util.Collections.singletonMap;

/**
 *
 */
public class MyAdminServlet extends HttpServlet
{
    private final TemplateRenderer templateRenderer;

    public MyAdminServlet(TemplateRenderer templateRenderer)
    {
        this.templateRenderer = templateRenderer;
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException
    {
        resp.setContentType("text/html");
        PrintWriter out = resp.getWriter();

        templateRenderer.render("test-page.vm", getRequestMap(req), out);
    }

    private Map<String, Object> getRequestMap(HttpServletRequest req)
    {
        Map<String,Object> result = newHashMap();
        for (String key : ((Collection<String>)req.getParameterMap().keySet()))
        {
            result.put(key, req.getParameter(key));
        }
        return result;
    }
}
