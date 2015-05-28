package com.atlassian.plugin.connect.core.capabilities.schema;

import java.io.IOException;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SchemaFilter implements Filter
{
    private static final Logger log = LoggerFactory.getLogger(SchemaFilter.class);
    public static final String JSON_SCHEMA_TYPE = "application/schema+json; charset=utf-8";
    
    private final ConnectSchemaLocator schemaLocator;

    public SchemaFilter(ConnectSchemaLocator schemaLocator)
    {
        this.schemaLocator = schemaLocator;
    }

    @Override
    public void init(FilterConfig filterConfig) throws ServletException
    {
    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException
    {
        HttpServletRequest req = (HttpServletRequest) servletRequest;
        HttpServletResponse res = (HttpServletResponse) servletResponse;
        boolean pretty = false;
        
        String prettyParam = servletRequest.getParameter("pretty");
        if (null != prettyParam && (prettyParam.length() == 0 || Boolean.parseBoolean(prettyParam)))
        {
            pretty = true;
        }

        String productPath = StringUtils.substringAfterLast(req.getRequestURI(), "/schema/");
        try
        {
            String schema;
            
            if(pretty)
            {
                schema = schemaLocator.getPrettySchema(productPath);
            }
            else
            {
                schema = schemaLocator.getSchema(productPath);
            }
            
            res.setContentType(JSON_SCHEMA_TYPE);
            res.setStatus(HttpServletResponse.SC_OK);
            res.setContentLength(schema.getBytes("UTF-8").length);
            ServletOutputStream sos = res.getOutputStream();
            sos.write(schema.getBytes());
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
