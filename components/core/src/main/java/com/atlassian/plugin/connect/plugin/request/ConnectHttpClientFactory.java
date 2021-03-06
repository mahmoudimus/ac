package com.atlassian.plugin.connect.plugin.request;

import com.atlassian.httpclient.api.HttpClient;
import com.atlassian.httpclient.api.factory.HttpClientFactory;
import com.atlassian.httpclient.api.factory.HttpClientOptions;
import com.atlassian.plugin.osgi.bridge.external.PluginRetrievalService;
import com.google.common.collect.Lists;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.annotation.Autowired;

import javax.inject.Named;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static com.google.common.base.Preconditions.checkNotNull;

@Named
public class ConnectHttpClientFactory implements DisposableBean {
    private final HttpClient httpClient;
    private final HttpClientFactory httpClientFactory;
    private final PluginRetrievalService pluginRetrievalService;
    private final List<HttpClient> instances = Lists.newArrayList();
    private final Logger log = LoggerFactory.getLogger(this.getClass());

    @Autowired
    public ConnectHttpClientFactory(HttpClientFactory httpClientFactory,
                                    PluginRetrievalService pluginRetrievalService) {
        this.pluginRetrievalService = checkNotNull(pluginRetrievalService);
        this.httpClientFactory = checkNotNull(httpClientFactory);
        this.httpClient = httpClientFactory.create(getHttpClientOptions());
        instances.add(httpClient);
    }

    @Override
    public void destroy() {
        for (HttpClient instance : instances) {
            try {
                httpClientFactory.dispose(instance);
            } catch (Exception e) {
                log.warn("Could not dispose of HttpClient", e);
            }
        }
    }

    public HttpClient getInstance() {
        return this.httpClient;
    }

    private HttpClientOptions getHttpClientOptions() {
        HttpClientOptions options = new HttpClientOptions();

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
