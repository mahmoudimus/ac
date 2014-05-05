package com.atlassian.plugin.connect.testsupport.filter;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A ServletFilter that can act as a remote addon inside of a wired test.
 * Essentially we can use this to serve addon requests without having to boot up jetty or something
 */
public class AddonTestFilter implements Filter
{
    private static final Pattern PATH_PATTERN = Pattern.compile("^/([^/]+)/([^/]+)/([^/]+)(?:/([^/]+))?");
    public static final String FILTER_MAPPING = "/ac-test-addon";
    public static final String RESOURCE_TIMEOUT = "timeout";
    public static final String RESOURCE_STATUS = "status";

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
            String parameter = matcher.group(4);

            testFilterResults.put(addOnKey + "/" + addonResource, new ServletRequestSnaphot(req));

            byte[] content = getContent(addonResource, parameter).getBytes("UTF-8");
            int statusCode = getStatusCode(addonResource, parameter);

            res.setStatus(statusCode);
            res.setContentLength(content.length);
            ServletOutputStream sos = res.getOutputStream();
            sos.write(content);
            sos.flush();
            sos.close();

            return;
        }

        res.sendError(HttpServletResponse.SC_NOT_FOUND);
    }

    private int getStatusCode(String addonResource, String parameter)
    {
        if (RESOURCE_STATUS.equals(addonResource))
        {
            return parameter == null ? HttpServletResponse.SC_OK : Integer.parseInt(parameter);
        }
        return HttpServletResponse.SC_OK;
    }

    private String getContent(String addonResource, String parameter)
    {
        if (RESOURCE_TIMEOUT.equals(addonResource))
        {
            long seconds = parameter == null ? 5 : Long.parseLong(parameter);
            waitSeconds(seconds);
        }
        return "hi";
    }

    private void waitSeconds(long timeout)
    {
        try
        {
            Thread.sleep(timeout * 1000);
        }
        catch (InterruptedException e)
        {
        }
    }

    @Override
    public void destroy()
    {
        //do nothing
    }
}
