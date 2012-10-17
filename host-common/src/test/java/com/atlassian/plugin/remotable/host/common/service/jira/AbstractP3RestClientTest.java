package com.atlassian.plugin.remotable.host.common.service.jira;

import com.atlassian.httpclient.api.Response;
import com.atlassian.httpclient.api.ResponsePromise;
import com.atlassian.httpclient.api.ResponsePromises;
import com.atlassian.jira.rest.client.RestClientException;
import com.atlassian.plugin.remotable.api.service.http.HostHttpClient;
import com.atlassian.util.concurrent.Effect;
import com.atlassian.util.concurrent.Promise;
import com.google.common.util.concurrent.SettableFuture;
import org.codehaus.jettison.json.JSONException;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.io.IOException;
import java.util.concurrent.ExecutionException;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;


@RunWith(MockitoJUnitRunner.class)
public final class AbstractP3RestClientTest
{
    private AbstractP3RestClient abstractP3RestClient;

    @Mock
    private HostHttpClient httpClient;

    private ResponsePromise responsePromise;

    private SettableFuture<Response> responseFuture;

    @Mock
    private Response response;

    @Mock
    private AbstractP3RestClient.ResponseHandler<Object> responseHandler;

    @Mock
    private Effect<Throwable> failEffect;

    private Object value;

    @Before
    public void setUp() throws Exception
    {
        responseFuture = SettableFuture.create();
        responsePromise = ResponsePromises.toResponsePromise(responseFuture);
        abstractP3RestClient = new AbstractP3RestClient(httpClient)
        {
        };

        value = new Object();
        when(responseHandler.handle(response)).thenReturn(value);
    }

    @Test
    public void testCallAndParseWhenResponseIs200Ok() throws Exception
    {
        testCallAndParseWithStatusCode(200, value);
    }

    @Test
    public void testCallAndParseWhenResponseIs201Created() throws Exception
    {
        testCallAndParseWithStatusCode(201, value);
    }

    @Test
    public void testCallAndParseWhenResponseIs404NotFound() throws Exception
    {
        final Promise<Object> promise = prepareWithResponseStatus(404);

        assertTrue(responsePromise.isDone());
        assertTrue(promise.isDone());

        assertNull(promise.get());
        verifyZeroInteractions(responseHandler, failEffect);
    }

    @Test
    public void testCallAndParseWhenResponseIsOther100() throws Exception
    {
        testCallAndParseWhenResponseIsOther(100);
    }

    @Test
    public void testCallAndParseWhenResponseIsOther300() throws Exception
    {
        testCallAndParseWhenResponseIsOther(300);
    }

    @Test
    public void testCallAndParseWhenResponseIsOther500() throws Exception
    {
        testCallAndParseWhenResponseIsOther(500);
    }

    @Test
    public void testCallAndParseWhenFailed() throws Exception
    {
        final Promise<Object> promise = abstractP3RestClient.callAndParse(responsePromise, responseHandler).fail(failEffect);

        assertFalse(responsePromise.isDone());
        assertFalse(promise.isDone());

        final Throwable throwable = new Throwable();
        responseFuture.setException(throwable);

        assertTrue(responsePromise.isDone());
        assertTrue(promise.isDone());

        verify(failEffect).apply(throwable);
    }

    @Test
    public void testCallWhenResponseIs204NoContent() throws Exception
    {
        final int statusCode = 204;

        when(response.getStatusCode()).thenReturn(statusCode);
        Promise<Void> promise = abstractP3RestClient.call(responsePromise).fail(failEffect);

        assertFalse(responsePromise.isDone());
        assertFalse(promise.isDone());

        responseFuture.set(response);

        assertTrue(responsePromise.isDone());
        assertTrue(promise.isDone());

        assertNull(promise.get());

        verifyZeroInteractions(failEffect);
    }

    @Test
    public void testCallWhenResponseIsOthers100() throws Exception
    {
        testCallWhenResponseIsOthers(100);
    }

    @Test
    public void testCallWhenResponseIsOthers200() throws Exception
    {
        testCallWhenResponseIsOthers(200);
    }

    @Test
    public void testCallWhenResponseIsOthers300() throws Exception
    {
        testCallWhenResponseIsOthers(300);
    }

    @Test
    public void testCallWhenResponseIsOthers400() throws Exception
    {
        testCallWhenResponseIsOthers(400);
    }

    @Test
    public void testCallWhenResponseIsOthers500() throws Exception
    {
        testCallWhenResponseIsOthers(500);
    }

    @Test
    public void testCallWhenFailed() throws Exception
    {
        final Promise<Void> promise = abstractP3RestClient.call(responsePromise).fail(failEffect);

        assertFalse(responsePromise.isDone());
        assertFalse(promise.isDone());

        final Throwable throwable = new Throwable();
        responseFuture.setException(throwable);

        assertTrue(responsePromise.isDone());
        assertTrue(promise.isDone());

        verify(failEffect).apply(throwable);
    }


    private void testCallWhenResponseIsOthers(int statusCode)
    {
        when(response.getStatusCode()).thenReturn(statusCode);
        Promise<Void> promise = abstractP3RestClient.call(responsePromise).fail(failEffect);

        assertFalse(responsePromise.isDone());
        assertFalse(promise.isDone());

        responseFuture.set(response);

        assertTrue(responsePromise.isDone());
        assertTrue(promise.isDone());

        verify(failEffect).apply(isA(RestClientException.class));
    }

    private void testCallAndParseWhenResponseIsOther(int statusCode)
    {
        final Promise<Object> promise = prepareWithResponseStatus(statusCode);

        assertTrue(responsePromise.isDone());
        assertTrue(promise.isDone());

        verify(failEffect).apply(Matchers.isA(RestClientException.class));
    }

    private void testCallAndParseWithStatusCode(int statusCode, Object expectedValue) throws JSONException, IOException, InterruptedException, ExecutionException
    {
        final Promise<Object> promise = prepareWithResponseStatus(statusCode);

        assertTrue(responsePromise.isDone());
        assertTrue(promise.isDone());

        verify(responseHandler).handle(response);
        assertSame(expectedValue, promise.get());

        verifyZeroInteractions(failEffect);
    }

    private Promise<Object> prepareWithResponseStatus(int statusCode)
    {
        when(response.getStatusCode()).thenReturn(statusCode);
        Promise<Object> promise = abstractP3RestClient.callAndParse(responsePromise, responseHandler).fail(failEffect);

        assertFalse(responsePromise.isDone());
        assertFalse(promise.isDone());

        responseFuture.set(response);

        return promise;
    }
}
