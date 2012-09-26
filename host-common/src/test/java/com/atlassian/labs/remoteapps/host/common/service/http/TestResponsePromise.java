package com.atlassian.labs.remoteapps.host.common.service.http;

import com.atlassian.labs.remoteapps.api.PromiseCallback;
import com.atlassian.labs.remoteapps.api.service.http.Request;
import com.atlassian.labs.remoteapps.api.service.http.Response;
import com.atlassian.labs.remoteapps.api.service.http.UnexpectedResponseException;
import com.google.common.util.concurrent.SettableFuture;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.runners.MockitoJUnitRunner;

import static com.atlassian.labs.remoteapps.api.service.http.ResponsePromises.toResponsePromise;
import static junit.framework.Assert.assertEquals;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class TestResponsePromise
{
    @Test
    public void testOthersAll()
    {
        PromiseCallback<Response> expected = mockSuccessCallback();
        ArgumentCaptor<Response> expectedCaptor = ArgumentCaptor.forClass(Response.class);
        PromiseCallback<Throwable> unexpectedFail = mockErrorCallback();

        newRequest(200).get()
            .done(expected)
            .others(expected)
            .fail(unexpectedFail);

        verify(expected, times(2)).handle(expectedCaptor.capture());
        assertEquals(200, expectedCaptor.getValue().getStatusCode());
        verify(unexpectedFail, never()).handle((Throwable) anyObject());
    }

    @Test
    public void testOthersUnexpected()
    {
        PromiseCallback<Response> expected = mockSuccessCallback();
        ArgumentCaptor<Response> expectedCaptor = ArgumentCaptor.forClass(Response.class);
        PromiseCallback<Response> unexpected = mockSuccessCallback();
        PromiseCallback<Throwable> unexpectedFail = mockErrorCallback();

        newRequest(200).get()
            .done(expected)
            .ok(expected)
            .others(unexpected)
            .fail(unexpectedFail);

        verify(expected, times(2)).handle(expectedCaptor.capture());
        assertEquals(200, expectedCaptor.getValue().getStatusCode());
        verify(unexpected, never()).handle((Response) anyObject());
        verify(unexpectedFail, never()).handle((Throwable) anyObject());
    }

    @Test
    public void testOthersExpected()
    {
        PromiseCallback<Response> expected = mockSuccessCallback();
        ArgumentCaptor<Response> expectedCaptor = ArgumentCaptor.forClass(Response.class);
        PromiseCallback<Response> unexpected = mockSuccessCallback();
        PromiseCallback<Throwable> unexpectedFail = mockErrorCallback();

        newRequest(201).get()
            .done(expected)
            .ok(unexpected)
            .others(expected)
            .fail(unexpectedFail);

        verify(expected, times(2)).handle(expectedCaptor.capture());
        assertEquals(201, expectedCaptor.getValue().getStatusCode());
        verify(unexpected, never()).handle((Response) anyObject());
        verify(unexpectedFail, never()).handle((Throwable) anyObject());
    }

    @Test
    public void testOtherwiseExpected()
    {
        PromiseCallback<Response> expected = mockSuccessCallback();
        ArgumentCaptor<Response> expectedCaptor = ArgumentCaptor.forClass(Response.class);
        PromiseCallback<Response> unexpected = mockSuccessCallback();
        PromiseCallback<Throwable> expectedFail = mockErrorCallback();
        ArgumentCaptor<Throwable> expectedFailCaptor = ArgumentCaptor.forClass(Throwable.class);
        PromiseCallback<Throwable> unexpectedFail = mockErrorCallback();

        newRequest(201).get()
            .done(expected)
            .ok(unexpected)
            .otherwise(expectedFail)
            .fail(unexpectedFail);

        verify(expected).handle(expectedCaptor.capture());
        assertEquals(201, expectedCaptor.getValue().getStatusCode());
        verify(unexpected, never()).handle((Response) anyObject());
        verify(expectedFail).handle(expectedFailCaptor.capture());
        assertEquals(UnexpectedResponseException.class, expectedFailCaptor.getValue().getClass());
        assertEquals(201, ((UnexpectedResponseException) expectedFailCaptor.getValue()).getResponse().getStatusCode());
        verify(unexpectedFail, never()).handle((Throwable) anyObject());
    }

    @Test
    public void testOtherwiseFailExpected()
    {
        PromiseCallback<Response> unexpected = mockSuccessCallback();
        PromiseCallback<Throwable> expectedFail = mockErrorCallback();
        ArgumentCaptor<Throwable> expectedFailCaptor = ArgumentCaptor.forClass(Throwable.class);

        newFailRequest().get()
            .done(unexpected)
            .ok(unexpected)
            .otherwise(expectedFail)
            .fail(expectedFail);

        verify(unexpected, never()).handle((Response) anyObject());
        verify(expectedFail, times(2)).handle(expectedFailCaptor.capture());
        assertEquals(RuntimeException.class, expectedFailCaptor.getValue().getClass());
        assertEquals("expected", expectedFailCaptor.getValue().getMessage());
    }

    @Test
    public void testOtherwiseUnexpected()
    {
        PromiseCallback<Response> expected = mockSuccessCallback();
        ArgumentCaptor<Response> expectedCaptor = ArgumentCaptor.forClass(Response.class);
        PromiseCallback<Response> unexpected = mockSuccessCallback();
        PromiseCallback<Throwable> unexpectedFail = mockErrorCallback();

        newRequest(200).get()
            .done(expected)
            .ok(expected)
            .otherwise(unexpectedFail)
            .fail(unexpectedFail);

        verify(expected, times(2)).handle(expectedCaptor.capture());
        assertEquals(200, expectedCaptor.getValue().getStatusCode());
        verify(unexpected, never()).handle((Response) anyObject());
        verify(unexpectedFail, never()).handle((Throwable) anyObject());
    }

    private Request newRequest(int code)
    {
        Request request = mock(Request.class);
        SettableFuture<Response> future = SettableFuture.create();
        when(request.get()).thenReturn(toResponsePromise(future));
        Response response = mock(Response.class);
        when(response.getStatusCode()).thenReturn(code);
        future.set(response);
        return request;
    }

    private Request newFailRequest()
    {
        Request request = mock(Request.class);
        SettableFuture<Response> future = SettableFuture.create();
        when(request.get()).thenReturn(toResponsePromise(future));
        future.setException(new RuntimeException("expected"));
        return request;
    }

    private PromiseCallback<Response> mockSuccessCallback()
    {
        return mock(PromiseCallback.class);
    }

    private PromiseCallback<Throwable> mockErrorCallback()
    {
        return mock(PromiseCallback.class);
    }
}
