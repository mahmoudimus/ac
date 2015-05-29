package com.atlassian.plugin.connect.core.util.http;

import com.atlassian.fugue.Option;
import com.atlassian.httpclient.api.HttpClient;
import com.atlassian.httpclient.api.Request;
import com.atlassian.httpclient.api.Response;
import com.atlassian.httpclient.api.ResponseTransformation;
import com.atlassian.plugin.connect.api.http.HttpMethod;
import com.atlassian.plugin.connect.api.util.UriBuilderUtils;
import com.atlassian.plugin.connect.api.util.http.ContentRetrievalErrors;
import com.atlassian.plugin.connect.api.util.http.ContentRetrievalException;
import com.atlassian.plugin.connect.core.ConnectHttpClientFactory;
import com.atlassian.plugin.connect.spi.http.AuthorizationGenerator;
import com.atlassian.plugin.connect.spi.util.http.HttpContentRetriever;
import com.atlassian.plugin.spring.scanner.annotation.export.ExportAsService;
import com.atlassian.uri.Uri;
import com.atlassian.uri.UriBuilder;
import com.atlassian.util.concurrent.Promise;
import com.google.common.base.Function;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Sets;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;
import javax.inject.Inject;
import javax.inject.Named;

import static com.google.common.base.Preconditions.checkState;
import static com.google.common.collect.Iterables.contains;
import static com.google.common.collect.Maps.newHashMap;

/**
 * Retrieves http content asynchronously and caches its contents in memory according to the returned headers
 */
@ExportAsService(HttpContentRetriever.class)
@Named
public final class CachingHttpContentRetriever implements HttpContentRetriever
{
    private final static Logger log = LoggerFactory.getLogger(CachingHttpContentRetriever.class);

    private static final Set<HttpMethod> METHODS_WITH_BODY = Sets.immutableEnumSet(HttpMethod.POST, HttpMethod.PUT);
    private static final Set<HttpMethod> METHODS_WITH_QUERY_PARAMS = Sets.immutableEnumSet(HttpMethod.GET);
    private static final Map<HttpMethod, Request.Method> METHOD_MAPPING = ImmutableMap.of(
            HttpMethod.GET, Request.Method.GET,
            HttpMethod.POST, Request.Method.POST,
            HttpMethod.PUT, Request.Method.PUT
    );

    private final HttpClient httpClient;

    @Inject
    public CachingHttpContentRetriever(ConnectHttpClientFactory httpClientFactory)
    {
        this.httpClient = httpClientFactory.getInstance();
    }

    @Override
    public void flushCacheByUriPattern(Pattern urlPattern)
    {
        httpClient.flushCacheByUriPattern(urlPattern);
    }

    @Override
    public Promise<String> async(AuthorizationGenerator authorizationGenerator,
                                 HttpMethod method,
                                 URI url,
                                 Map<String, String[]> parameters,
                                 Map<String, String> headers,
                                 String addOnKey)
    {
        checkState(METHOD_MAPPING.keySet().contains(method), "The only valid methods are: %s", METHOD_MAPPING.keySet());

        log.debug("{}ing content from '{}'", method, url);

        Request.Builder request = httpClient.newRequest(getFullUrl(method, url, parameters));
        request = request.setAttributes(getAttributes(addOnKey));
        Option<String> authHeaderValue = getAuthHeaderValue(authorizationGenerator, method, url, parameters);
        Map<String, String> allHeaders = getAllHeaders(headers, authHeaderValue);
        request = request.setHeaders(allHeaders);

        if (contains(METHODS_WITH_BODY, method))
        {
            request.setContentType("application/x-www-form-urlencoded");
            request.setEntity(UriBuilder.joinParameters(UriBuilderUtils.toListFormat(parameters)));
        }

        ResponseTransformation<String> responseTransformation = httpClient.<String>transformation()
                .ok(new OkFunction(url))
                .forbidden(new ForbiddenFunction(url))
                .others(new OthersFunction(url))
                .fail(new FailFunction(url))
                .build();
        return request.execute(METHOD_MAPPING.get(method)).transform(responseTransformation);
    }

    private String getFullUrl(HttpMethod method, URI url, Map<String, String[]> allParameters)
    {
        final UriBuilder uriBuilder = new UriBuilder(Uri.fromJavaUri(url));
        if (contains(METHODS_WITH_QUERY_PARAMS, method))
        {
            UriBuilderUtils.addQueryParameters(uriBuilder, allParameters);
        }
        return uriBuilder.toString();
    }

    private Map<String, String> getAttributes(String addOnKey)
    {
        final Map<String, String> properties = newHashMap();
        properties.put("purpose", "content-retrieval");
        properties.put("moduleKey", addOnKey);
        return properties;
    }

    private Map<String, String> getAllHeaders(Map<String, String> headers, Option<String> authHeader)
    {
        final ImmutableMap.Builder<String, String> allHeaders = ImmutableMap.<String, String>builder().putAll(headers);
        if (authHeader.isDefined())
        {
            allHeaders.put("Authorization", authHeader.get());
        }
        return allHeaders.build();
    }

    private Option<String> getAuthHeaderValue(AuthorizationGenerator authorizationGenerator, HttpMethod method, URI url, Map<String, String[]> allParameters)
    {
        return authorizationGenerator.generate(method, url, allParameters);
    }

    private static class OkFunction implements Function<Response, String>
    {
        private final URI url;

        public OkFunction(URI url)
        {
            this.url = url;
        }

        @Override
        public String apply(Response input)
        {
            log.debug("Returned ok content from: {}", url);
            return input.getEntity();
        }
    }

    private static class OthersFunction implements Function<Response, String>
    {
        private final URI url;

        public OthersFunction(URI url)
        {
            this.url = url;
        }

        @Override
        public String apply(Response input)
        {
            log.debug("Returned others: {}", url);
            if ("application/json".equalsIgnoreCase(input.getContentType()))
            {
                throw new ContentRetrievalException(ContentRetrievalErrors.fromJson(input.getEntity()));
            }
            else
            {
                log.debug("An unknown error occurred retrieving HTTP content. Status is {}, body content " +
                        "is:\n{}\n", input.getStatusCode(), input.getEntity());
                throw new ContentRetrievalException("Unknown error. Status is " + input.getStatusCode());
            }
        }
    }

    private static final class ForbiddenFunction implements Function<Response, String>
    {
        private final URI url;

        public ForbiddenFunction(URI url)
        {
            this.url = url;
        }

        @Override
        public String apply(Response input)
        {
            log.debug("Returned forbidden: {}", url);
            throw new ContentRetrievalException("Operation not authorized!");
        }
    }

    private static final class FailFunction implements Function<Throwable, String>
    {
        private final URI url;

        public FailFunction(URI url)
        {
            this.url = url;
        }

        @Override
        public String apply(Throwable input)
        {
            if (!(input instanceof ContentRetrievalException))
            {
                log.debug("Return failed: {}", url);
                log.debug(input.getMessage(), input);
                throw new ContentRetrievalException(input);
            }
            else
            {
                throw (ContentRetrievalException) input;
            }
        }
    }
}
