package com.atlassian.plugin.connect.plugin;

import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.inject.Named;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.annotation.Autowired;

import com.atlassian.httpclient.api.HttpClient;
import com.atlassian.httpclient.api.factory.HttpClientFactory;
import com.atlassian.httpclient.api.factory.HttpClientOptions;
import com.atlassian.plugin.osgi.bridge.external.PluginRetrievalService;
import com.atlassian.sal.api.features.DarkFeatureManager;
import com.atlassian.sal.api.features.EnabledDarkFeatures;
import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

import static com.google.common.base.Preconditions.checkNotNull;

@Named
public class ConnectHttpClientFactory implements DisposableBean
{
    private static final String DHE_DISABLED_DARK_FEATURE_PREFIX = "atlassian.connect.dhe.disabled.";
    private final HttpClient httpClient;
    private final HttpClientFactory httpClientFactory;
    private final PluginRetrievalService pluginRetrievalService;
    private final DarkFeatureManager darkFeatureManager;
    private final List<HttpClient> instances = Lists.newArrayList();
    private final Logger log = LoggerFactory.getLogger(this.getClass());

    @Autowired
    public ConnectHttpClientFactory(DarkFeatureManager darkFeatureManager, HttpClientFactory httpClientFactory,
        PluginRetrievalService pluginRetrievalService)
    {
        this.darkFeatureManager = checkNotNull(darkFeatureManager);
        this.pluginRetrievalService = checkNotNull(pluginRetrievalService);
        this.httpClientFactory = checkNotNull(httpClientFactory);
        this.httpClient = httpClientFactory.create(getHttpClientOptions());
        instances.add(httpClient);
    }

    @Override
    public void destroy()
    {
        for (HttpClient instance : instances)
        {
            try
            {
                httpClientFactory.dispose(instance);
            }
            catch (Exception e)
            {
                log.warn("Could not dispose of HttpClient", e);
            }
        }
    }

    public HttpClient getInstance()
    {
        return this.httpClient;
    }

    private List<String> getNonDHEHosts()
    {
        EnabledDarkFeatures enabled = darkFeatureManager.getFeaturesEnabledForAllUsers();
        Iterable<String> nonDHEHostFeatures = Iterables.filter(enabled.getFeatureKeys(), new Predicate<String>()
        {

            @Override
            public boolean apply(String feature)
            {
                return feature.startsWith(DHE_DISABLED_DARK_FEATURE_PREFIX);
            }
        });
        return Lists.newArrayList(Iterables.transform(nonDHEHostFeatures, new Function<String, String>()
        {

            @Override
            public String apply(String input)
            {
                return input.replace(DHE_DISABLED_DARK_FEATURE_PREFIX, "");
            }
        }));
    }

    private HttpClientOptions getHttpClientOptions()
    {
        HttpClientOptions options = new HttpClientOptions();

        options.setDheDisabledHosts(getNonDHEHosts());

        options.setIoSelectInterval(100, TimeUnit.MILLISECONDS);
        options.setThreadPrefix("atlassian-connect");
        options.setMaxConnectionsPerHost(100);
        options.setUserAgent("Atlassian-Connect/"
                             + pluginRetrievalService.getPlugin().getPluginInformation().getVersion());

        options.setConnectionTimeout(3, TimeUnit.SECONDS);
        options.setSocketTimeout(5, TimeUnit.SECONDS);
        options.setRequestTimeout(10, TimeUnit.SECONDS);
        options.setLeaseTimeout(TimeUnit.SECONDS.toMillis(3));
        return options;
    }
}
