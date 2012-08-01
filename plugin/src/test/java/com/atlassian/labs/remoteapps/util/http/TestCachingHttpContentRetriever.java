package com.atlassian.labs.remoteapps.util.http;

import com.atlassian.labs.remoteapps.api.services.http.impl.RequestKiller;
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
    UserManager userManager;
    
    @Mock
    PluginRetrievalService pluginRetrievalService;
    
    @Mock
    Plugin plugin;

    @Mock
    AuthorizationGenerator authorizationGenerator;

    private CachingHttpContentRetriever retriever;

    @Before
    public void setUp() throws NoSuchFieldException, IllegalAccessException, IOException
    {
        initMocks(this);

        when(plugin.getPluginInformation()).thenReturn(new PluginInformation());
        when(pluginRetrievalService.getPlugin()).thenReturn(plugin);
        RequestKiller killer = new RequestKiller();
        this.retriever = new CachingHttpContentRetriever(userManager, pluginRetrievalService, killer);
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
        when(userManager.getRemoteUsername()).thenReturn("bob");
        ArgumentCaptor<HttpPost> argument = ArgumentCaptor.forClass(HttpPost.class);
        retriever.postIgnoreResponse(authorizationGenerator, "http://localhost/foo", "{\"boo\":\"bar\"}");
        verify(httpClient).execute(argument.capture(), ArgumentCaptor.forClass(FutureCallback.class).capture());
        assertEquals("http://localhost/foo", argument.getValue().getURI().toString());
    }
    @Test
    public void testPostIgnoreResponseNoUser() throws IOException
    {
        when(userManager.getRemoteUsername()).thenReturn(null);
        ArgumentCaptor<HttpPost> argument = ArgumentCaptor.forClass(HttpPost.class);
        retriever.postIgnoreResponse(authorizationGenerator, "http://localhost/foo", "{\"boo\":\"bar\"}");
        verify(httpClient).execute(argument.capture(), ArgumentCaptor.forClass(FutureCallback.class).capture());
        assertEquals("http://localhost/foo", argument.getValue().getURI().toString());
    }

}
