package com.atlassian.plugin.remotable.host.common.service.http;

import com.atlassian.util.concurrent.Effect;
import com.atlassian.util.concurrent.Promise;
import com.atlassian.util.concurrent.Promises;
import com.atlassian.xmlrpc.XmlRpcClientProvider;
import com.google.common.util.concurrent.SettableFuture;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.lang.reflect.Method;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public final class PromiseAwareXmlRpcInvocationHandlerTest
{
    @Mock
    private XmlRpcClientProvider xmlRpcClientProvider;

    private PromiseAwareXmlRpcInvocationHandler invocationHandler;

    private Promise<Object> promise;

    private SettableFuture<Object> future;

    @Mock
    private Effect<Object> doneEffect;

    @Mock
    private Effect<Throwable> failEffect;

    @Before
    public void setUp() throws Exception
    {
        invocationHandler = new PromiseAwareXmlRpcInvocationHandler(xmlRpcClientProvider);
        future = SettableFuture.create();
        promise = Promises.forListenableFuture(future);
    }

    @Test
    public void testConvertReturnValueWhenDone() throws Exception
    {
        final Method method = this.getClass().getMethods()[0];
        final Promise<Object> returnedPromise = ((Promise<Object>) invocationHandler.convertReturnValue(method, promise))
                .done(doneEffect)
                .fail(failEffect);

        assertFalse(returnedPromise.isDone());

        final Object value = new Object();
        future.set(value);

        assertTrue(returnedPromise.isDone());
        verify(doneEffect).apply(value);
        verifyZeroInteractions(failEffect);
    }

    @Test
    public void testConvertReturnValueWhenFailed() throws Exception
    {
        final Method method = this.getClass().getMethods()[0];
        final Promise<Object> returnedPromise = ((Promise<Object>) invocationHandler.convertReturnValue(method, promise))
                .done(doneEffect)
                .fail(failEffect);

        assertFalse(returnedPromise.isDone());

        final Throwable throwable = new Throwable();
        future.setException(throwable);

        assertTrue(returnedPromise.isDone());
        verifyZeroInteractions(doneEffect);
        verify(failEffect).apply(throwable);
    }
}
