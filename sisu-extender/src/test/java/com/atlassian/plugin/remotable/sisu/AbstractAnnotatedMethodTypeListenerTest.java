package com.atlassian.plugin.remotable.sisu;

import com.google.inject.spi.TypeEncounter;
import org.junit.Before;
import org.junit.Test;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public final class AbstractAnnotatedMethodTypeListenerTest
{
    private AbstractAnnotatedMethodTypeListener listener;

    @Before
    public void setUp()
    {
        listener = new AbstractAnnotatedMethodTypeListener(TestAnnotation.class)
        {
            @Override
            protected <I> void hear(Method method, TypeEncounter<I> encounter)
            {
                // no-op
            }
        };
    }

    @Test
    public void testObjectIsJdkType()
    {
        assertTrue(listener.isJdkType(Object.class));
    }

    @Test
    public void testNullIsNotJdkType()
    {
        assertFalse(listener.isJdkType(null));
    }

    @Test
    public void testThisClassIsNotJdkType()
    {
        assertFalse(listener.isJdkType(this.getClass()));
    }

    @Test
    public void testProxyClassIsNotJdkType()
    {
        assertFalse(listener.isJdkType(proxy(TestInterface.class).getClass()));
    }

    @Test
    public void testProxyIsProxy()
    {
        assertTrue(listener.isProxy(proxy(TestInterface.class).getClass()));
    }

    @Test
    public void testObjectIsNotProxy()
    {
        assertFalse(listener.isProxy(Object.class));
    }

    @Test
    public void testNullIsNotProxy()
    {
        assertFalse(listener.isProxy(null));
    }

    private static Object proxy(Class<?> proxiedInterface)
    {
        return Proxy.newProxyInstance(
                proxiedInterface.getClassLoader(),
                new Class[]{proxiedInterface},
                new InvocationHandler()
                {
                    @Override
                    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable
                    {
                        throw new UnsupportedOperationException("Not implemented");
                    }
                });
    }

    private static @interface TestAnnotation
    {
    }

    private static interface TestInterface
    {
    }
}
