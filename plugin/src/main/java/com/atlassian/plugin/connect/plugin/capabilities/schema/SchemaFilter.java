package com.atlassian.plugin.connect.plugin.capabilities.schema;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.core.MediaType;

import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.osgi.bridge.external.PluginRetrievalService;
import com.atlassian.plugin.spring.scanner.ProductFilter;
import com.atlassian.sal.api.ApplicationProperties;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;

public class SchemaFilter implements Filter
{
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
    
            res.setContentType(MediaType.APPLICATION_JSON);
            res.setStatus(HttpServletResponse.SC_OK);
            res.setContentLength(data.length);
            ServletOutputStream sos = res.getOutputStream();
            sos.write(data);
            sos.flush();
            sos.close();
        }
        catch (Exception e)
        {
            send404("/schema/" + productPath,res);
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
