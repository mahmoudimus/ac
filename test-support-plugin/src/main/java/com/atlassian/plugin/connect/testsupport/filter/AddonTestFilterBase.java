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
import java.util.LinkedList;
import java.util.Queue;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.atlassian.fugue.Option;
import com.atlassian.sal.api.user.UserManager;
import org.apache.commons.lang3.StringUtils;

/**
 * A ServletFilter that can act as a remote addon inside of a wired test.
 * Essentially we can use this to serve addon requests without having to boot up jetty or something.
 */
public abstract class AddonTestFilterBase implements Filter
{
    private static final Pattern PATH_PATTERN = Pattern.compile("^/([^/]+)/([^/]+)/([^/]+)(?:/([^/]+))?");
    public static final String FILTER_MAPPING = "/ac-test-addon";
    public static final String RESOURCE_TIMEOUT = "timeout";
    public static final String RESOURCE_STATUS = "status";

    private final AddonTestFilterResults testFilterResults;
    private final UserManager userManager;
    private final AddonPrecannedResponseHelper addonPrecannedResponseHelper;

    public AddonTestFilterBase(AddonTestFilterResults testFilterResults, UserManager userManager,
                               AddonPrecannedResponseHelper addonPrecannedResponseHelper)
    {
        this.testFilterResults = testFilterResults;
        this.userManager = userManager;
        this.addonPrecannedResponseHelper = addonPrecannedResponseHelper;
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

        if (shouldProcess(req))
        {
            String pathInfo = req.getServletPath();
            Matcher matcher = PATH_PATTERN.matcher(pathInfo);
            if (matcher.find())
            {
                String addOnKey = matcher.group(2);
                String addonResource = matcher.group(3);
                String parameter = matcher.group(4);

                testFilterResults.put(addOnKey + "/" + addonResource, new ServletRequestSnaphot(req, userManager));

                Option<PrecannedResponse> precannedResponse = addonPrecannedResponseHelper.poll();

                byte[] content = getContent(addonResource, parameter).getBytes("UTF-8");
                int statusCode = getStatusCode(addonResource, parameter, precannedResponse, pathInfo);

                res.setStatus(statusCode);
                res.setContentLength(content.length);
                ServletOutputStream sos = res.getOutputStream();
                sos.write(content);
                sos.flush();
                sos.close();

                return;
            }
        }

        processNonMatch(req, res, filterChain);
    }

    protected abstract boolean shouldProcess(HttpServletRequest request);
    protected abstract void processNonMatch(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws IOException, ServletException;

    private int getStatusCode(String addonResource, String parameter, Option<PrecannedResponse> precannedResponse,
                              String path)
    {
        if (RESOURCE_STATUS.equals(addonResource))
        {
            if (parameter != null)
            {
                if (precannedResponse.isDefined())
                {
                    throw new IllegalStateException("Cannot specify a status code when a precanned response is present");
                }
                return Integer.parseInt(parameter);
            }
        }

        return precannedResponse.isDefined()  && StringUtils.endsWith(path, precannedResponse.get().getRequiredPath())
                ? precannedResponse.get().getStatusCode() : HttpServletResponse.SC_OK;
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
            // just move along
        }
    }

    @Override
    public void destroy()
    {
        //do nothing
    }

}
