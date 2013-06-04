package com.atlassian.plugin.remotable.plugin.util.http;

import com.atlassian.httpclient.api.HttpClient;
import com.atlassian.httpclient.api.Response;
import com.atlassian.httpclient.api.factory.HttpClientFactory;
import com.atlassian.httpclient.api.factory.HttpClientOptions;
import com.atlassian.plugin.osgi.bridge.external.PluginRetrievalService;
import com.atlassian.plugin.remotable.plugin.license.LicenseRetriever;
import com.atlassian.plugin.remotable.plugin.util.LocaleHelper;
import com.atlassian.plugin.remotable.spi.http.AuthorizationGenerator;
import com.atlassian.sal.api.message.LocaleResolver;
import com.atlassian.uri.Uri;
import com.atlassian.uri.UriBuilder;
import com.atlassian.util.concurrent.Promise;
import com.google.common.base.Function;
import com.google.common.collect.Maps;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

import static com.google.common.collect.Maps.newHashMap;
import static java.util.Collections.singletonList;

/**
 * Retrieves http content asynchronously and caches its contents in memory according to the returned headers
 */
public class CachingHttpContentRetriever implements HttpContentRetriever
{
    private final Logger log = LoggerFactory.getLogger(CachingHttpContentRetriever.class);
    private final HttpClient httpClient;
    private final LicenseRetriever licenseRetriever;
    private final LocaleHelper localeHelper;

    public CachingHttpContentRetriever(PluginRetrievalService pluginRetrievalService,
            HttpClientFactory httpClientFactory, final LicenseRetriever licenseRetriever, LocaleHelper localeHelper)
    {
        this.licenseRetriever = licenseRetriever;
        this.localeHelper = localeHelper;
        HttpClientOptions options = new HttpClientOptions();
        options.setIoSelectInterval(100, TimeUnit.MILLISECONDS);
        options.setThreadPrefix("content-ret");
        options.setMaxConnectionsPerHost(100);
        options.setUserAgent("Atlassian-Remotable-Plugins/"
                + pluginRetrievalService.getPlugin().getPluginInformation().getVersion());

        options.setConnectionTimeout(3, TimeUnit.SECONDS);
        options.setSocketTimeout(15, TimeUnit.SECONDS);
        options.setRequestTimeout(20, TimeUnit.SECONDS);

        this.httpClient = httpClientFactory.create(options);
    }

    @Override
    public void flushCacheByUriPattern(Pattern urlPattern)
    {
        httpClient.flushCacheByUriPattern(urlPattern);
    }

    @Override
    public Promise<String> getAsync(final AuthorizationGenerator authorizationGenerator, final String remoteUsername,
            final URI url,
            final Map<String, String> parameters, Map<String, String> headers,
            final String pluginKey)
    {
        final Map<String, String> queryParams = newHashMap(parameters);
        queryParams.put("lic", licenseRetriever.getLicenseStatus(pluginKey).value());
        queryParams.put("loc", localeHelper.getLocaleTag());

        final String urlWithParams = new UriBuilder(Uri.fromJavaUri(url))
                .addQueryParameters(queryParams)
                .toString();

        String authHeaderValue = authorizationGenerator.generate(
                "GET", url,
                Maps.transformValues(queryParams, new Function<String, List<String>>()
                {
                    @Override
                    public List<String> apply(String from)
                    {
                        return singletonList(from);
                    }
                }));

        Map<String, String> newHeaders = newHashMap(headers);
        if (authHeaderValue != null)
        {
            newHeaders.put("Authorization", authHeaderValue);
        }

        final Map<String, String> properties = Maps.newHashMap();
        properties.put("purpose", "content-retrieval");
        properties.put("moduleKey", pluginKey);

        log.info("Retrieving content from '{}' for user '{}'", new Object[]{url, remoteUsername});

        Promise<String> promise = httpClient.newRequest(urlWithParams)
                .setHeaders(newHeaders)
                .setAttributes(properties)
                .get()
                .<String>transform()
                .ok(new Function<Response, String>()
                {
                    @Override
                    public String apply(Response input)
                    {
                        log.debug("Returned ok content from: {}", url);
                        return input.getEntity();
                    }
                })
                .forbidden(new Function<Response, String>()
                {
                    @Override
                    public String apply(Response input)
                    {
                        log.debug("Returned forbidden: {}", url);
                        throw new ContentRetrievalException("Operation not authorized!");
                    }
                })
                .others(new Function<Response, String>()
                {
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
                })
                .fail(new Function<Throwable, String>()
                {
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
                })
                .toPromise();

        if (promise.isDone())
        {
            log.debug("Request {} retrieved from the cache", url);
        }
        else
        {
            log.debug("Request {} isn't in the cache, retrieving...", url);
        }
        return promise;
    }
}
