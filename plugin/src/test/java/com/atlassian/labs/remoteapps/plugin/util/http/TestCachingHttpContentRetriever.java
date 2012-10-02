package com.atlassian.labs.remoteapps.plugin.util.http;

import org.apache.commons.lang.StringUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.junit.Test;

import java.io.IOException;
import java.util.concurrent.ExecutionException;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 *
 */
public class TestCachingHttpContentRetriever
{
    @Test
    public void testRetrievalWithinLimits() throws ExecutionException, InterruptedException, IOException
    {
        HttpResponse response = mock(HttpResponse.class);
        HttpEntity entity = mock(HttpEntity.class);
        when(response.getEntity()).thenReturn(entity);
        when(entity.getContentLength()).thenReturn(1024L);
        when(entity.getContent()).thenReturn(new GeneratingInputStream('x', 1024L));
        String data = CachingHttpContentRetriever.ResponseToStringFuture.responseToString(response);
        assertEquals(StringUtils.repeat("x", 1024), data);
    }

    @Test
    public void testRetrievalWithinLimitsNoLength() throws ExecutionException, InterruptedException, IOException
    {
        HttpResponse response = mock(HttpResponse.class);
        HttpEntity entity = mock(HttpEntity.class);
        when(response.getEntity()).thenReturn(entity);
        when(entity.getContentLength()).thenReturn(-1L);
        when(entity.getContent()).thenReturn(new GeneratingInputStream('x', 1024L));
        String data = CachingHttpContentRetriever.ResponseToStringFuture.responseToString(response);
        assertEquals(StringUtils.repeat("x", 1024), data);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testRetrievalOutsideLimitsWithLength() throws ExecutionException, InterruptedException, IOException
    {
        HttpResponse response = mock(HttpResponse.class);
        HttpEntity entity = mock(HttpEntity.class);
        when(response.getEntity()).thenReturn(entity);
        when(entity.getContentLength()).thenReturn(1024*1024*101L);
        when(entity.getContent()).thenReturn(new GeneratingInputStream('x', 1024*1024*101L));
        CachingHttpContentRetriever.ResponseToStringFuture.responseToString(response);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testRetrievalOutsideLimitsNoLength() throws ExecutionException, InterruptedException, IOException
    {
        HttpResponse response = mock(HttpResponse.class);
        HttpEntity entity = mock(HttpEntity.class);
        when(response.getEntity()).thenReturn(entity);
        when(entity.getContentLength()).thenReturn(-1L);
        when(entity.getContent()).thenReturn(new GeneratingInputStream('x', 1024*1024*101L));
        CachingHttpContentRetriever.ResponseToStringFuture.responseToString(response);
    }
}
