package com.atlassian.labs.remoteapps.util.http;

import com.atlassian.applinks.api.ApplicationLink;
import com.atlassian.labs.remoteapps.OAuthLinkManager;
import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.PluginInformation;
import com.atlassian.plugin.osgi.bridge.external.PluginRetrievalService;
import com.atlassian.sal.api.user.UserManager;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.concurrent.FutureCallback;
import org.apache.http.impl.client.cache.CachingHttpAsyncClient;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Matchers;
import org.mockito.Mock;

import java.io.IOException;
import java.util.concurrent.Future;

import static junit.framework.Assert.assertEquals;
import static org.mockito.Mockito.*;
import static org.mockito.MockitoAnnotations.initMocks;

public class TestCachingHttpContentRetriever
{
    @Mock
    CachingHttpAsyncClient httpClient;
    
    @Mock
    OAuthLinkManager oAuthLinkManager;
    
    @Mock
    UserManager userManager;
    
    @Mock
    PluginRetrievalService pluginRetrievalService;
    
    @Mock
    Plugin plugin;
    private CachingHttpContentRetriever retriever;

    @Before
    public void setUp() throws NoSuchFieldException, IllegalAccessException, IOException
    {
        initMocks(this);

        when(plugin.getPluginInformation()).thenReturn(new PluginInformation());
        when(pluginRetrievalService.getPlugin()).thenReturn(plugin);
        RequestTimeoutKiller killer = new RequestTimeoutKiller();
        this.retriever = new CachingHttpContentRetriever(oAuthLinkManager,
                userManager, pluginRetrievalService, killer);
        this.retriever.httpClient = httpClient;
        HttpResponse response = mock(HttpResponse.class);
        StatusLine status = mock(StatusLine.class);
        when(status.getStatusCode()).thenReturn(200);
        when(response.getStatusLine()).thenReturn(status);
        Future future = mock(Future.class);
        when(httpClient.execute(Matchers.<HttpUriRequest>any(), Matchers.<FutureCallback>any())).thenReturn(future);
    }
    
    @Test
    public void testPostIgnoreResponse() throws IOException
    {
        ApplicationLink link = mock(ApplicationLink.class);
        when(userManager.getRemoteUsername()).thenReturn("bob");
        ArgumentCaptor<HttpPost> argument = ArgumentCaptor.forClass(HttpPost.class);
        retriever.postIgnoreResponse(link, "http://localhost/foo", "{\"boo\":\"bar\"}");
        verify(httpClient).execute(argument.capture(), ArgumentCaptor.forClass(FutureCallback.class).capture());
        assertEquals("http://localhost/foo", argument.getValue().getURI().toString());
    }
    @Test
    public void testPostIgnoreResponseNoUser() throws IOException
    {
        ApplicationLink link = mock(ApplicationLink.class);
        when(userManager.getRemoteUsername()).thenReturn(null);
        ArgumentCaptor<HttpPost> argument = ArgumentCaptor.forClass(HttpPost.class);
        retriever.postIgnoreResponse(link, "http://localhost/foo", "{\"boo\":\"bar\"}");
        verify(httpClient).execute(argument.capture(), ArgumentCaptor.forClass(FutureCallback.class).capture());
        assertEquals("http://localhost/foo", argument.getValue().getURI().toString());
    }

}
