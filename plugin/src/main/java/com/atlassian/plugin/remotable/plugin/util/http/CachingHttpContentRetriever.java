package com.atlassian.plugin.remotable.plugin.util.http;

import com.atlassian.fugue.Option;
import com.atlassian.httpclient.api.HttpClient;
import com.atlassian.httpclient.api.Request;
import com.atlassian.httpclient.api.Response;
import com.atlassian.httpclient.api.factory.HttpClientFactory;
import com.atlassian.httpclient.api.factory.HttpClientOptions;
import com.atlassian.plugin.osgi.bridge.external.PluginRetrievalService;
import com.atlassian.plugin.remotable.plugin.license.LicenseRetriever;
import com.atlassian.plugin.remotable.plugin.util.LocaleHelper;
import com.atlassian.plugin.remotable.plugin.util.function.MapFunctions;
import com.atlassian.plugin.remotable.spi.http.AuthorizationGenerator;
import com.atlassian.uri.Uri;
import com.atlassian.uri.UriBuilder;
import com.atlassian.util.concurrent.Promise;
import com.google.common.base.Function;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

import static com.atlassian.fugue.Option.option;
import static com.atlassian.httpclient.api.Request.Method;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.collect.Iterables.contains;

/**
 * Retrieves http content asynchronously and caches its contents in memory according to the returned headers
 */
public final class CachingHttpContentRetriever implements HttpContentRetriever
{
    private final static Logger log = LoggerFactory.getLogger(CachingHttpContentRetriever.class);

    private static final ImmutableSet<Method> METHODS_WITH_BODY = Sets.immutableEnumSet(Method.POST, Method.PUT, Method.TRACE);
    private static final ImmutableSet<Method> METHODS_WITH_QUERY_PARAMS = Sets.immutableEnumSet(Method.GET, Method.DELETE, Method.HEAD);

    private final HttpClient httpClient;
    private final LicenseRetriever licenseRetriever;
    private final LocaleHelper localeHelper;

    public CachingHttpContentRetriever(LicenseRetriever licenseRetriever, LocaleHelper localeHelper, HttpClientFactory httpClientFactory, PluginRetrievalService pluginRetrievalService)
    {
        this(licenseRetriever, localeHelper, httpClientFactory, getHttpClientOptions(checkNotNull(pluginRetrievalService, "pluginRetrievalService")));
    }

    CachingHttpContentRetriever(LicenseRetriever licenseRetriever, LocaleHelper localeHelper, HttpClientFactory httpClientFactory, HttpClientOptions httpClientOptions)
    {
        this(licenseRetriever, localeHelper, checkNotNull(httpClientFactory, "httpClientFactory").create(checkNotNull(httpClientOptions, "httpClientOptions")));
    }

    CachingHttpContentRetriever(LicenseRetriever licenseRetriever, LocaleHelper localeHelper, HttpClient httpClient)
    {
        this.licenseRetriever = checkNotNull(licenseRetriever);
        this.localeHelper = checkNotNull(localeHelper);
        this.httpClient = checkNotNull(httpClient);
    }

    @Override
    public void flushCacheByUriPattern(Pattern urlPattern)
    {
        httpClient.flushCacheByUriPattern(urlPattern);
    }

    @Override
    public Promise<String> async(AuthorizationGenerator authorizationGenerator,
                                 Method method,
                                 URI url,
                                 Map<String, String> parameters,
                                 Map<String, String> headers,
                                 String pluginKey)
    {
        log.info("{}ing content from '{}'", method, url);

        final Map<String, String> allParameters = getAllParameters(parameters, pluginKey);

        final Request request = httpClient.newRequest(getFullUrl(method, url, allParameters))
                .setHeaders(getAllHeaders(headers, getAuthHeaderValue(authorizationGenerator, method, url, allParameters)))
                .setAttributes(getAttributes(pluginKey));

        if (contains(METHODS_WITH_BODY, method))
        {
            request.setContentType("application/x-www-form-urlencoded");
            request.setEntity(UriBuilder.joinParameters(transformParameters(allParameters)));
        }

        return request.execute(method)
                .<String>transform()
                .ok(new OkFunction(url))
                .forbidden(new ForbiddenFunction(url))
                .others(new OthersFunction(url))
                .fail(new FailFunction(url))
                .toPromise();
    }

    private String getFullUrl(Method method, URI url, Map<String, String> allParameters)
    {
        final UriBuilder uriBuilder = new UriBuilder(Uri.fromJavaUri(url));
        if (contains(METHODS_WITH_QUERY_PARAMS, method))
        {
            uriBuilder.addQueryParameters(allParameters);
        }
        return uriBuilder.toString();
    }

    private Map<String, String> getAttributes(String pluginKey)
    {
        final Map<String, String> properties = Maps.newHashMap();
        properties.put("purpose", "content-retrieval");
        properties.put("moduleKey", pluginKey);
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

    private Option<String> getAuthHeaderValue(AuthorizationGenerator authorizationGenerator, Method method, URI url, Map<String, String> allParameters)
    {
        return option(authorizationGenerator.generate(method.name(), url, transformParameters(allParameters)));
    }

    private Map<String, List<String>> transformParameters(Map<String, String> allParameters)
    {
        return Maps.transformValues(allParameters, MapFunctions.STRING_TO_LIST);
    }

    private Map<String, String> getAllParameters(Map<String, String> parameters, String pluginKey)
    {
        return ImmutableMap.<String, String>builder()
                .putAll(parameters)
                .put("lic", getLicenseStatusAsString(pluginKey))
                .put("loc", getLocale()).build();
    }

    private String getLicenseStatusAsString(String pluginKey)
    {
        return licenseRetriever.getLicenseStatus(pluginKey).value();
    }

    private String getLocale()
    {
        return localeHelper.getLocaleTag();
    }

    @Override
    public Promise<String> getAsync(AuthorizationGenerator authorizationGenerator,
                                    String remoteUsername,
                                    URI url,
                                    Map<String, String> parameters,
                                    Map<String, String> headers,
                                    String pluginKey)
    {
        return async(authorizationGenerator, Method.GET, url, parameters, headers, pluginKey);
    }

    private static HttpClientOptions getHttpClientOptions(PluginRetrievalService pluginRetrievalService)
    {
        HttpClientOptions options = new HttpClientOptions();
        options.setIoSelectInterval(100, TimeUnit.MILLISECONDS);
        options.setThreadPrefix("http-content-retriever");
        options.setMaxConnectionsPerHost(100);
        options.setUserAgent("Atlassian-Remotable-Plugins/" + pluginRetrievalService.getPlugin().getPluginInformation().getVersion());

        options.setConnectionTimeout(3, TimeUnit.SECONDS);
        options.setSocketTimeout(15, TimeUnit.SECONDS);
        options.setRequestTimeout(20, TimeUnit.SECONDS);
        return options;
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
                throw new ContentRetrievalException("An unknown error occurred!");
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
