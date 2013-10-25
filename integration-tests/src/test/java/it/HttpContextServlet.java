package it;

import java.io.IOException;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;

import static com.atlassian.fugue.Option.option;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Strings.nullToEmpty;

public class HttpContextServlet extends HttpServlet
{
    private final Map<String, Object> baseContext = Maps.newHashMap();
    private final ContextServlet servlet;

    public HttpContextServlet(ContextServlet servlet)
    {
        this.servlet = checkNotNull(servlet);
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException
    {
        servlet.doGet(req, resp, getContext(req));
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException
    {
        servlet.doPost(req, resp, getContext(req));
    }

    private ImmutableMap<String, Object> getContext(HttpServletRequest req) throws IOException
    {
        return ImmutableMap.<String, Object>builder()
                           .putAll(baseContext)
                           .put("req_url", nullToEmpty(option(req.getRequestURL()).getOrElse(new StringBuffer()).toString()))
                           .put("req_uri", nullToEmpty(req.getRequestURI()))
                           .put("req_query", nullToEmpty(req.getQueryString()))
                           .put("req_method", req.getMethod())
                           .put("clientKey", nullToEmpty(req.getParameter("oauth_consumer_key")))
                           .put("locale", nullToEmpty(req.getParameter("loc")))
                           .put("licenseStatus", nullToEmpty(req.getParameter("lic")))
                           .put("timeZone", nullToEmpty(req.getParameter("tz")))
                           .build();
    }

    public Map<String, Object> getBaseContext()
    {
        return baseContext;
    }
}