package com.atlassian.plugin.connect.jira.capabilities.descriptor.search;

import com.atlassian.httpclient.api.HttpClient;
import com.atlassian.httpclient.api.Request;
import com.atlassian.jira.search.SearchResultItem;
import com.atlassian.plugin.connect.api.module.webfragment.UrlVariableSubstitutor;
import com.atlassian.plugin.connect.plugin.webhooks.RemotePluginRequestSigner;
import com.atlassian.plugin.spring.scanner.annotation.component.JiraComponent;
import com.atlassian.plugin.spring.scanner.annotation.imports.ComponentImport;
import com.google.common.collect.ImmutableMap;
import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import org.springframework.beans.factory.annotation.Autowired;

import java.net.URI;
import java.util.Collections;
import java.util.List;

@JiraComponent
public class WebSearchRequestSender {

    private final Gson gson = new Gson();

    private final RemotePluginRequestSigner requestSigner;
    private final HttpClient httpClient;
    private final UrlVariableSubstitutor variableSubstitutor;

    @Autowired
    public WebSearchRequestSender(final RemotePluginRequestSigner requestSigner, @ComponentImport final HttpClient httpClient, final UrlVariableSubstitutor variableSubstitutor) {
        this.requestSigner = requestSigner;
        this.httpClient = httpClient;
        this.variableSubstitutor = variableSubstitutor;
    }

    public List<SearchResultItem> send(ConnectWebSearcherModuleDescriptor.ConnectWebSearcher webSearcher, String query) {
        String urlStr = variableSubstitutor.replace(webSearcher.getUrl(), ImmutableMap.of("search.query", query));
        URI uri = URI.create(urlStr);
        Request.Builder request = httpClient.newRequest(uri);
        requestSigner.sign(uri, webSearcher.getPluginKey(), request);

        try {
            String response = request.get().get().getEntity();
            Object result = gson.fromJson(response, new TypeToken<List<SearchResultItem>>() {}.getType());
            return (List<SearchResultItem>) result;
        } catch (Exception e) {
            return Collections.emptyList();
        }
    }
}
