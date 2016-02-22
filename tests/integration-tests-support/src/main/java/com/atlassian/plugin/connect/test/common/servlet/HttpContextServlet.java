package com.atlassian.plugin.connect.test.common.servlet;

import java.io.IOException;
import java.util.Collections;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.atlassian.plugin.connect.api.request.HttpMethod;

import com.google.common.collect.ImmutableMap;

import org.apache.commons.io.IOUtils;

import static com.atlassian.fugue.Option.option;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Strings.nullToEmpty;
import static com.google.common.collect.Maps.newHashMap;

public class HttpContextServlet extends HttpServlet {
    private final Map<String, Object> baseContext = newHashMap();
    private final ContextServlet servlet;
    private final Iterable<FormParameterExtractor> formParameterExtractors;
    private final Iterable<BodyExtractor> bodyExtractors;

    public HttpContextServlet(ContextServlet servlet) {
        this(servlet, Collections.emptyList());
    }

    public HttpContextServlet(ContextServlet servlet, Iterable<FormParameterExtractor> extractors) {
        this(checkNotNull(servlet), extractors, Collections.emptyList());
    }

    public HttpContextServlet(ContextServlet servlet, Iterable<FormParameterExtractor> extractors, Iterable<BodyExtractor> bodyExtractors) {
        this.servlet = checkNotNull(servlet);
        this.formParameterExtractors = extractors;
        this.bodyExtractors = bodyExtractors;
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        servlet.doGet(req, resp, getContext(req));
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        servlet.doPost(req, resp, getContext(req));
    }

    private ImmutableMap<String, Object> getContext(HttpServletRequest req) throws IOException {
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

    private Map<String, ?> extractContext(final HttpServletRequest req) {
        final ImmutableMap.Builder<String, Object> builder = ImmutableMap.builder();
        for (FormParameterExtractor extractor : formParameterExtractors) {
            final String extractedValue = extractor.extract(req);
            if (extractedValue != null) {
                builder.put(extractor.getParameterId(), extractedValue);
            }
        }
        if (req.getMethod().equalsIgnoreCase(HttpMethod.POST.name())) {
            for (BodyExtractor bodyExtractor : bodyExtractors) {
                try {
                    builder.putAll(bodyExtractor.extractAll(IOUtils.toString(req.getInputStream())));
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        }
        return builder.build();
    }

    public Map<String, Object> getBaseContext() {
        return baseContext;
    }
}