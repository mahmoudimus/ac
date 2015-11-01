package it.servlet;

import com.google.common.collect.ImmutableMap;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Map;

import static com.atlassian.fugue.Option.option;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Strings.nullToEmpty;
import static com.google.common.collect.Maps.newHashMap;

public class HttpContextServlet extends HttpServlet
{
    private final Map<String, Object> baseContext = newHashMap();
    private final ContextServlet servlet;
    private final Iterable<TestServletContextExtractor> servletContextExtractors;

    public HttpContextServlet(ContextServlet servlet)
    {
        this(servlet, new ArrayList<TestServletContextExtractor>());
    }

    public HttpContextServlet(ContextServlet servlet, Iterable<TestServletContextExtractor> extractors)
    {
        this.servlet = checkNotNull(servlet);
        this.servletContextExtractors = extractors;
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
                           .putAll(extractContext(req))
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

    private Map<String, ?> extractContext(final HttpServletRequest req)
    {
        final ImmutableMap.Builder<String, Object> builder = ImmutableMap.builder();
        for (TestServletContextExtractor extractor : servletContextExtractors)
        {
            final Object extractedValue = extractor.extract(req);
            if (extractedValue != null)
            {
                builder.put(extractor.getParameterId(), extractedValue);
            }
        }
        return builder.build();
    }

    public Map<String, Object> getBaseContext()
    {
        return baseContext;
    }
}