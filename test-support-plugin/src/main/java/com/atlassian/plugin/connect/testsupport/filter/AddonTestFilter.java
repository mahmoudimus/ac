package com.atlassian.plugin.connect.testsupport.filter;

import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * A ServletFilter that can act as a remote addon inside of a wired test.
 * Essentially we can use this to serve addon requests without having to boot up jetty or something
 */
public class AddonTestFilter implements Filter
{
    private static final Pattern PATH_PATTERN = Pattern.compile("^/([^/]+)/([^/]+)/([^/]+)");
    public static final String FILTER_MAPPING = "/ac-test-addon";

    private final AddonTestFilterResults testFilterResults;
    
    public AddonTestFilter(AddonTestFilterResults testFilterResults)
    {
        this.testFilterResults = testFilterResults;
    }

    private FilterConfig config;

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
        String pathInfo = req.getServletPath();
        Matcher matcher = PATH_PATTERN.matcher(pathInfo);
        if (matcher.find())
        {
            String addOnKey = matcher.group(2);
            String addonResource = matcher.group(3);

            testFilterResults.put(addOnKey + "/" + addonResource, new ServletRequestSnaphot(req));

            res.setStatus(HttpServletResponse.SC_OK);
            res.setContentLength(2);
            ServletOutputStream sos = res.getOutputStream();
            sos.write("hi".getBytes());
            sos.flush();
            sos.close();

            return;
        }

        res.sendError(HttpServletResponse.SC_NOT_FOUND);
    }

    @Override
    public void destroy()
    {
        //do nothing
    }
}
