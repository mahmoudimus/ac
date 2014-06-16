package com.atlassian.plugin.connect.testsupport.filter;

import com.atlassian.sal.api.user.UserManager;
import com.atlassian.sal.api.user.UserProfile;
import org.apache.commons.io.IOUtils;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

public class ServletRequestSnaphot
{
    private final String contextPath;
    private final String servletPath;
    private final String pathInfo;
    private final String requestUri;
    private final StringBuffer requestURL;
    private final Map<String, Object> attributes = new HashMap<String, Object>();
    private final Map<String, String> headers = new HashMap<String, String>();
    private final Map<String, String[]> parameters = new HashMap<String, String[]>();
    private final String method;
    private final String serverName;
    private final int serverPort;
    private final String entity;
    private final String remoteUsername;

    public ServletRequestSnaphot(HttpServletRequest request, UserManager userManager)
    {
        this.contextPath = request.getContextPath();
        this.servletPath = request.getServletPath();
        this.pathInfo = request.getPathInfo();
        this.requestUri = request.getRequestURI();
        this.requestURL = request.getRequestURL();
        this.method = request.getMethod();
        this.serverName = request.getServerName();
        this.serverPort = request.getServerPort();

        Enumeration<String> attrNames = request.getAttributeNames();
        while(attrNames.hasMoreElements())
        {
            String attrName = attrNames.nextElement();
            attributes.put(attrName,request.getAttribute(attrName));
        }

        Enumeration<String> headerNames = request.getHeaderNames();
        while(headerNames.hasMoreElements())
        {
            String headerName = headerNames.nextElement();
            headers.put(headerName,request.getHeader(headerName));
        }

        Enumeration<String> paramNames = request.getParameterNames();
        while(paramNames.hasMoreElements())
        {
            String paramName = paramNames.nextElement();
            parameters.put(paramName,request.getParameterValues(paramName));
        }
        
        this.entity = extractEntity(request);
        final UserProfile remoteUser = userManager.getRemoteUser(request);
        this.remoteUsername = null == remoteUser ? null : remoteUser.getUsername();
    }

    private static String extractEntity(HttpServletRequest request)
    {
        try
        {
            return IOUtils.toString(request.getInputStream());
        }
        catch (IOException e)
        {
            return "";
        }
    }

    public String getContextPath()
    {
        return contextPath;
    }

    public String getServletPath()
    {
        return servletPath;
    }

    public String getPathInfo()
    {
        return pathInfo;
    }

    public String getRequestUri()
    {
        return requestUri;
    }

    public StringBuffer getRequestURL()
    {
        return requestURL;
    }

    public Map<String, Object> getAttributes()
    {
        return attributes;
    }

    public Map<String, String> getHeaders()
    {
        return headers;
    }

    public Map<String, String[]> getParameters()
    {
        return parameters;
    }

    public String getMethod()
    {
        return method.toUpperCase();
    }

    public String getServerName()
    {
        return serverName;
    }

    public int getServerPort()
    {
        return serverPort;
    }

    public String getEntity()
    {
        return entity;
    }

    public String getRemoteUsername()
    {
        return remoteUsername;
    }
}
