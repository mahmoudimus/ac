package com.atlassian.plugin.connect.plugin;

import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.connect.plugin.util.PathBuilder;
import com.atlassian.plugin.connect.api.util.UriBuilderUtils;
import com.atlassian.plugin.connect.spi.util.http.HttpContentRetriever;
import com.atlassian.plugin.connect.spi.RemotablePluginAccessor;
import com.atlassian.plugin.connect.spi.http.HttpMethod;
import com.atlassian.uri.Uri;
import com.atlassian.uri.UriBuilder;
import com.atlassian.util.concurrent.Promise;
import com.google.common.base.Supplier;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.httpclient.NameValuePair;
import org.apache.commons.httpclient.util.ParameterParser;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.util.List;
import java.util.Map;

import static com.google.common.base.Preconditions.checkNotNull;

public abstract class DefaultRemotablePluginAccessorBase implements RemotablePluginAccessor
{
    private final String pluginKey;
    private final String pluginName;
    private final Supplier<URI> baseUrlSupplier;
    private final HttpContentRetriever httpContentRetriever;

    private static final Logger log = LoggerFactory.getLogger(DefaultRemotablePluginAccessorBase.class);
    private static final char QUERY_PARAM_SEPARATOR = '&';

    protected DefaultRemotablePluginAccessorBase(Plugin plugin, Supplier<URI> baseUrlSupplier, HttpContentRetriever httpContentRetriever)
    {
        this(plugin.getKey(), plugin.getName(), baseUrlSupplier, httpContentRetriever);
    }

    protected DefaultRemotablePluginAccessorBase(String pluginKey, String pluginName, Supplier<URI> baseUrlSupplier, HttpContentRetriever httpContentRetriever)
    {
        this.pluginKey = pluginKey;
        this.pluginName = pluginName;
        this.baseUrlSupplier = baseUrlSupplier;
        this.httpContentRetriever = httpContentRetriever;
    }

    @Override
    public String createGetUrl(URI targetPath, Map<String, String[]> params)
    {
        UriBuilder uriBuilder = new UriBuilder(Uri.fromJavaUri(getTargetUrl(targetPath)));
        UriBuilderUtils.addQueryParameters(uriBuilder, params);
        return uriBuilder.toString();
    }

    @Override
    public Promise<String> executeAsync(HttpMethod method,
                                        URI targetPath,
                                        Map<String, String[]> params,
                                        Map<String, String> headers)
    {
        return httpContentRetriever.async(getAuthorizationGenerator(),
                method,
                getTargetUrl(targetPath),
                params,
                headers,
                getKey());
    }

    @Override
    public String getKey()
    {
        return pluginKey;
    }

    @Override
    public String getName()
    {
        return pluginName;
    }

    @Override
    public URI getBaseUrl()
    {
        return baseUrlSupplier.get();
    }

    public URI getTargetUrl(URI targetPath)
    {
        if (targetPath.isAbsolute())
        {
            throw new IllegalArgumentException("Target url was absolute (" + targetPath.toString() + "). Expected relative path to base URL of add-on (" + getBaseUrl().toString() + ").");
        }

        UriBuilder uriBuilder = new UriBuilder(Uri.fromJavaUri(getBaseUrl()));
        String path = new PathBuilder()
                .withPathFragment(uriBuilder.getPath())
                .withPathFragment(targetPath.getRawPath())
                .build();
        uriBuilder.setPath(path);
        uriBuilder.setQuery(targetPath.getRawQuery());

        return uriBuilder.toUri().toJavaUri();
    }

    // Call this method to ensure that you don't have "some_param=<any value>" in targetPath and also "some_param" => [ <any values> ] in params.
    // This is to protect against duplicate result values ("some_param=1&some_param=1"), contradictory values ("some_param=1&some_param=2") and not knowing which (if any) is correct.
    protected void assertThatTargetPathAndParamsDoNotDuplicateParams(URI targetPath, Map<String, String[]> params)
    {
        checkNotNull(targetPath);

        if (!MapUtils.isEmpty(params))
        {
            List queryParams = new ParameterParser().parse(targetPath.getQuery(), QUERY_PARAM_SEPARATOR);

            for (Object queryParam : queryParams)
            {
                if (queryParam instanceof NameValuePair)
                {
                    NameValuePair nameValuePair = (NameValuePair) queryParam;

                    if (params.containsKey(nameValuePair.getName()))
                    {
                        throw new IllegalArgumentException(String.format("targetPath and params arguments both contain a parameter called '%s'. " +
                                "This is ambiguous (which takes precedence? is it a mistake?). Please supply this parameters in one or the other. targetPath = '%s', params['%s'] = [%s]",
                                nameValuePair.getName(), targetPath.getQuery(), nameValuePair.getName(), StringUtils.join(params.get(nameValuePair.getName()), ',')));
                    }
                }
                else
                {
                    log.warn("Ignoring query parameter '{}' that is of type '{}' rather than the expected NameValuePair", queryParam, null == queryParam ? null : queryParam.getClass().getName());
                }
            }
        }
    }
}
