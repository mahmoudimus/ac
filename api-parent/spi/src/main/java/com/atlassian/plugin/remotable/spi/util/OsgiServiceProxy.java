package com.atlassian.plugin.remotable.spi.util;

import org.osgi.framework.BundleContext;
import org.osgi.util.tracker.ServiceTracker;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.concurrent.TimeUnit;

public final class OsgiServiceProxy
{
    private static final int SERVICE_WAIT_SECONDS = 20;

    /**
     * Wraps the service in a dynamic proxy that ensures all methods are executed with the object class's class loader
     * as the context class loader
     */
    public static <T> T wrapService(BundleContext consumingBundleContext, Class<T> interfaceClass, ClassLoader consumerClassLoader)
    {
        return interfaceClass.cast(
                Proxy.newProxyInstance(consumerClassLoader, new Class[]{interfaceClass},
                        new ServiceTrackerInvocationHandler<T>(consumingBundleContext, interfaceClass)));
    }

    /**
     * InvocationHandler for a dynamic proxy that ensures all methods are executed with the
     * object class's class loader as the context class loader.
     */
    private static final class ServiceTrackerInvocationHandler<T> implements InvocationHandler
    {
        private final ServiceTracker serviceTracker;
        private final Class<T> interfaceClass;

        public ServiceTrackerInvocationHandler(BundleContext consumingBundleContext, Class<T> interfaceClass
        )
        {
            this.interfaceClass = interfaceClass;
            this.serviceTracker = new ServiceTracker(consumingBundleContext, interfaceClass.getName(), null);
            this.serviceTracker.open();
        }

        public Object invoke(final Object o, final Method method, final Object[] objects) throws Throwable
        {
            try
            {
                final T instance = interfaceClass.cast(serviceTracker.waitForService(TimeUnit.SECONDS.toMillis(SERVICE_WAIT_SECONDS)));
                if (instance == null)
                {
                    throw new IllegalStateException("Service not available within " + SERVICE_WAIT_SECONDS + " seconds: '" + interfaceClass.getName() + "'");
                }
                return method.invoke(instance, objects);
            }
            catch (final InvocationTargetException e)
            {
                throw e.getTargetException();
            }
        }

        @Override
        protected void finalize() throws Throwable
        {
            this.serviceTracker.close();
            super.finalize();
        }
    }
}
