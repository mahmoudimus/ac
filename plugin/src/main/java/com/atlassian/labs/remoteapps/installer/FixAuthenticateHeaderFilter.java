package com.atlassian.labs.remoteapps.installer;

import java.io.IOException;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;

/**
 * Fixes the authentication header to allows a BASIC WWW-Authenticate header value
 */
public class FixAuthenticateHeaderFilter implements Filter
{
    public void init(FilterConfig filterConfig) throws ServletException
    {
    }

    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException
    {
        if ("basic".equals(request.getParameter("os_authType")))
        {
            chain.doFilter(request, new BasicWWWAuthenticateAddingResponse((HttpServletResponse) response));
        }
        else
        {
            chain.doFilter(request, response);
        }
    }

    public void destroy()
    {
    }

    /**
     * Wraps a HttpServletResponse and listens for the status to be set to a "401 Not authorized" or a 401 error to
     * be sent so that it can add the WWW-Authenticate headers for Speakeasy.  Necessary because of OAuth filter.
     */
    private static final class BasicWWWAuthenticateAddingResponse extends HttpServletResponseWrapper
    {
        public BasicWWWAuthenticateAddingResponse(HttpServletResponse response)
        {
            super(response);
        }

        @Override
        public void addHeader(String name, String value)
        {
            if (name.equals("WWW-Authenticate"))
            {
                addBasicAuthenticateHeader();
            }
            else
            {
                super.addHeader(name, value);
            }
        }

        private void addBasicAuthenticateHeader()
        {
            super.addHeader("WWW-Authenticate", "Basic realm=\"Atlassian Remote Apps server\"");
        }
    }

}
