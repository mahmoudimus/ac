package com.atlassian.plugin.remotable.host.common.service.http;

import com.atlassian.httpclient.api.Request;
import com.atlassian.httpclient.api.Response;
import com.atlassian.httpclient.api.ResponsePromises;
import com.atlassian.httpclient.api.ResponseTransformationException;
import com.atlassian.httpclient.api.UnexpectedResponseException;
import com.atlassian.plugin.remotable.api.service.http.HostHttpClient;
import com.atlassian.plugin.remotable.api.service.http.XmlRpcException;
import com.atlassian.plugin.remotable.api.service.http.XmlRpcFault;
import com.atlassian.util.concurrent.Effect;
import com.atlassian.util.concurrent.Promise;
import com.google.common.base.Objects;
import com.google.common.util.concurrent.SettableFuture;
import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;
import redstone.xmlrpc.XmlRpcStruct;

import java.io.InputStream;
import java.io.StringWriter;
import java.net.URI;

import static org.junit.Assert.*;
import static org.mockito.Matchers.*;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public final class DefaultHostXmlRpcClientTest
{
    private DefaultHostXmlRpcClient client;

    @Mock
    private HostHttpClient httpClient;

    @Mock
    private Request request;

    @Mock
    private Effect<ExpectedClass> doneEffect;

    @Mock
    private Effect<Throwable> failEffect;

    private SettableFuture<Response> responseFuture;

    @Mock
    private Response response;

    @Mock
    private FaultHandlingXmlRpcParser parser;

    @Before
    public void setUp()
    {
        client = new DefaultHostXmlRpcClient(httpClient);
        when(httpClient.newRequest(Mockito.<URI>any())).thenReturn(request);
        when(request.setContentType(anyString())).thenReturn(request);
        when(request.setContentCharset(anyString())).thenReturn(request);
        when(request.setEntity(anyString())).thenReturn(request);

        responseFuture = SettableFuture.create();
        when(request.post()).thenReturn(ResponsePromises.toResponsePromise(responseFuture));
    }

    @Test
    public final void testEndCallOnResponseOk200AndParsesFine()
    {
        final Promise<ExpectedClass> promise = client
                .endCall(new StringWriter(), parser, ExpectedClass.class)
                .done(doneEffect)
                .fail(failEffect);

        assertFalse(promise.isDone());

        when(response.getStatusCode()).thenReturn(200);

        final ExpectedClass parsedValue = mock(ExpectedClass.class);
        when(parser.getParsedValue()).thenReturn(parsedValue);

        responseFuture.set(response);

        assertTrue(promise.isDone());

        verify(doneEffect).apply(parsedValue);
        verifyZeroInteractions(failEffect);
    }

    @Test
    public final void testEndCallOnResponseOk200AndParsesNotExpectedClass()
    {
        final Promise<ExpectedClass> promise = client
                .endCall(new StringWriter(), parser, ExpectedClass.class)
                .done(doneEffect)
                .fail(failEffect);

        assertFalse(promise.isDone());

        when(response.getStatusCode()).thenReturn(200);

        final Object parsedValue = new Object();
        when(parser.getParsedValue()).thenReturn(parsedValue);

        responseFuture.set(response);

        assertTrue(promise.isDone());

        verifyZeroInteractions(doneEffect);
        verify(failEffect).apply(isA(XmlRpcException.class));
    }

    @Test
    public final void testEndCallOnResponseOk200AndParsesXmlFault()
    {
        final Promise<ExpectedClass> promise = client
                .endCall(new StringWriter(), parser, ExpectedClass.class)
                .done(doneEffect)
                .fail(failEffect);

        assertFalse(promise.isDone());

        when(response.getStatusCode()).thenReturn(200);
        when(parser.isFaultResponse()).thenReturn(true);
        when(parser.getParsedValue()).thenReturn(mock(XmlRpcStruct.class));

        responseFuture.set(response);

        assertTrue(promise.isDone());

        verifyZeroInteractions(doneEffect);
        verify(failEffect).apply(isA(XmlRpcFault.class));
    }

    @Test
    public final void testEndCallOnResponseOk200AndParsesWithException()
    {
        final Promise<ExpectedClass> promise = client
                .endCall(new StringWriter(), parser, ExpectedClass.class)
                .done(doneEffect)
                .fail(failEffect);

        assertFalse(promise.isDone());

        when(response.getStatusCode()).thenReturn(200);
        doThrow(new RuntimeException()).when(parser).parse(Mockito.<InputStream>any());

        responseFuture.set(response);

        assertTrue(promise.isDone());

        verifyZeroInteractions(doneEffect);
        verify(failEffect).apply(isA(XmlRpcException.class));
    }

    @Test
    public final void testEndCallOnResponseOtherThan200()
    {
        final Promise<ExpectedClass> promise = client
                .endCall(new StringWriter(), new FaultHandlingXmlRpcParser(), ExpectedClass.class)
                .done(doneEffect)
                .fail(failEffect);

        assertFalse(promise.isDone());

        when(response.getStatusCode()).thenReturn(100);
        responseFuture.set(response);
        assertTrue(promise.isDone());

        verifyZeroInteractions(doneEffect);
        verify(failEffect).apply(isAnUnexpectedResponseException(response));
    }

    @Test
    public final void testEndCallOnResponseFail()
    {
        final Promise<ExpectedClass> promise = client
                .endCall(new StringWriter(), new FaultHandlingXmlRpcParser(), ExpectedClass.class)
                .done(doneEffect)
                .fail(failEffect);

        assertFalse(promise.isDone());

        final Throwable throwable = new Throwable();

        responseFuture.setException(throwable);
        assertTrue(promise.isDone());

        verifyZeroInteractions(doneEffect);
        verify(failEffect).apply(isA(ResponseTransformationException.class));
    }

    private static UnexpectedResponseException isAnUnexpectedResponseException(Response response)
    {
        return argThat(new UnexpectedResponseExceptionMatcher(response));
    }

    private static class UnexpectedResponseExceptionMatcher extends BaseMatcher<UnexpectedResponseException>
    {
        private final Response response;

        private UnexpectedResponseExceptionMatcher(Response response)
        {
            this.response = response;
        }

        @Override
        public boolean matches(Object o)
        {
            if (!(o instanceof UnexpectedResponseException))
            {
                return false;
            }

            final UnexpectedResponseException e = (UnexpectedResponseException) o;
            return Objects.equal(response, e.getResponse());
        }

        @Override
        public void describeTo(Description description)
        {
            description.appendText("Unexpected response exception.");
        }
    }

    private static interface ExpectedClass
    {
    }
}
