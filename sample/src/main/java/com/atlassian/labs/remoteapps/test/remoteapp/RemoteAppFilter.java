package com.atlassian.labs.remoteapps.test.remoteapp;

import com.atlassian.templaterenderer.TemplateRenderer;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;

import javax.servlet.*;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.StringWriter;
import java.nio.charset.Charset;
import java.util.Map;

import static com.google.common.collect.Lists.newArrayList;

/**
 *
 */
public class RemoteAppFilter implements Filter
{
    private static TemplateRenderer templateRenderer;
    private final AutowireCapableBeanFactory beanFactory;
    private Multimap<String,Route> allRoutes = ArrayListMultimap.create();

    public RemoteAppFilter(TemplateRenderer templateRenderer, AutowireCapableBeanFactory beanFactory)
    {
        this.templateRenderer = templateRenderer;
        this.beanFactory = beanFactory;
    }

    @Override
    public void init(FilterConfig filterConfig) throws ServletException
    {
        post(new RegisterRoute("/register"));
        get(new MyAdminRoute("/myadmin"));
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException
    {
        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse resp = (HttpServletResponse) response;
        String path = req.getRequestURI().substring(req.getContextPath().length() + "/remoteapp".length());
        boolean handled = false;
        for (Route route : allRoutes.get(req.getMethod()))
        {
            if (path.equals(route.path))
            {
                String output = route.handle(req, resp);
                if (output != null)
                {
                    byte[] bytes = output.getBytes(Charset.forName("UTF-8"));
                    resp.setContentLength(bytes.length);
                    resp.getOutputStream().write(bytes);
                    resp.getOutputStream().close();
                }
                handled = true;
                break;
            }
        }
        if (!handled)
        {
            resp.sendError(404, "Unable to match path " + path + " for method " + req.getMethod());
        }
    }

    @Override
    public void destroy()
    {
        allRoutes.clear();
    }


    public static abstract class Route
    {
        private final String path;

        public Route(String path)
        {
            this.path = path;
        }

        protected String render(String templatePath, Map<String,Object> context) throws IOException
        {
            StringWriter writer = new StringWriter();
            templateRenderer.render(templatePath, context, writer);
            return writer.toString();
        }

        abstract String handle(HttpServletRequest req, HttpServletResponse resp) throws IOException, ServletException;
    }
    private void get(Route route)
    {
        allRoutes.put("GET", route);
    }

    private void post(Route route)
    {
        allRoutes.put("POST", route);
    }

    private void put(Route route)
    {
        allRoutes.put("POST", route);
    }

    private void delete(Route route)
    {
        allRoutes.put("POST", route);
    }
}
