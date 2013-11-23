package com.atlassian.plugin.connect.plugin.capabilities.schema;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.osgi.bridge.external.PluginRetrievalService;
import com.atlassian.plugin.spring.scanner.ProductFilter;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SchemaFilter implements Filter
{
    private static final Logger log = LoggerFactory.getLogger(SchemaFilter.class);

    public static final String JSON_SCHEMA_TYPE = "application/schema+json";
    public static final String RAW = "/schema/%s-schema.json";
    public static final String PRETTY = "/schema/%s-schema-pretty.json";

    private FilterConfig config;

    private final Plugin plugin;


    public SchemaFilter(PluginRetrievalService pluginRetrievalService)
    {
        this.plugin = pluginRetrievalService.getPlugin();
    }

    @Override
    public void init(FilterConfig filterConfig) throws ServletException
    {
        this.config = filterConfig;
    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException
    {
        HttpServletRequest req = (HttpServletRequest) servletRequest;
        HttpServletResponse res = (HttpServletResponse) servletResponse;
        String jsonFormat;

        if (null != servletRequest.getParameter("pretty"))
        {
            jsonFormat = PRETTY;
        }
        else
        {
            jsonFormat = RAW;
        }

        String productPath = StringUtils.substringAfterLast(req.getRequestURI(), "/schema/");

        try
        {
            ProductFilter requestedProduct = ProductFilter.valueOf(productPath.toUpperCase());

            String path = String.format(jsonFormat, requestedProduct.name().toLowerCase());
            System.out.println("path = " + path);

            ByteArrayOutputStream bout = new ByteArrayOutputStream();
            InputStream in = plugin.getResourceAsStream(path);
            IOUtils.copy(in, bout);
            byte[] data = bout.toByteArray();

            res.setContentType(JSON_SCHEMA_TYPE);
            res.setStatus(HttpServletResponse.SC_OK);
            res.setContentLength(data.length);
            ServletOutputStream sos = res.getOutputStream();
            sos.write(data);
            sos.flush();
            sos.close();
        }
        catch (Exception e)
        {
            log.error("Unable to find connect schema at path: /schema/" + productPath, e);
            send404("/schema/" + productPath, res);
        }
    }

    private void send404(String path, HttpServletResponse res) throws IOException
    {
        res.sendError(HttpServletResponse.SC_NOT_FOUND, "Cannot find resource: " + path);
    }

    @Override
    public void destroy()
    {
        //nothing to do
    }
}
