package com.atlassian.labs.remoteapps.apputils.spring;

import com.atlassian.fugue.Option;
import com.google.common.base.Function;
import com.google.common.base.Supplier;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.FactoryBean;

import static com.atlassian.fugue.Option.*;
import static com.google.common.base.Preconditions.*;
import static com.google.common.base.Suppliers.*;

/**
 * A generic component factory bean that checks for the type availability before looking for
 * the corresponding OSGi service.
 */
public final class ComponentFactoryBean implements FactoryBean, DisposableBean
{
    private final BundleContext bundleContext;
    private final String type;

    private final Supplier<Option<ServiceReference>> reference;

    public ComponentFactoryBean(BundleContext bundleContext, String type)
    {
        this.bundleContext = checkNotNull(bundleContext);
        this.type = checkNotNull(type);
        this.reference = memoize(synchronizedSupplier(new ServiceReferenceOptionSupplier(bundleContext, type)));
    }

    @Override
    public Object getObject() throws Exception
    {
        return reference.get().fold(Undefined.supplier(type), new ServiceFunction<Object>());
    }

    @Override
    public Class getObjectType()
    {
        try
        {
            return this.getClass().getClassLoader().loadClass(type);
        }
        catch (ClassNotFoundException e)
        {
            return Undefined.class;
        }
    }

    @Override
    public void destroy() throws Exception
    {
        reference.get().map(new Function<ServiceReference, Boolean>()
        {
            @Override
            public Boolean apply(ServiceReference reference)
            {
                return bundleContext.ungetService(reference);
            }
        });
    }

    @Override
    public boolean isSingleton()
    {
        return true;
    }

    static final class Undefined
    {
        private final String type;

        Undefined(String type)
        {
            this.type = checkNotNull(type);
        }

        static Supplier<Undefined> supplier(final String type)
        {
            return new Supplier<Undefined>()
            {
                @Override
                public Undefined get()
                {
                    return new Undefined(type);
                }
            };
        }
    }

    private static class ServiceReferenceOptionSupplier implements Supplier<Option<ServiceReference>>
    {
        private final BundleContext bundleContext;
        private final String type;

        public ServiceReferenceOptionSupplier(BundleContext bundleContext, String type)
        {
            this.bundleContext = checkNotNull(bundleContext);
            this.type = checkNotNull(type);
        }

        @Override
        public Option<ServiceReference> get()
        {
            return option(bundleContext.getServiceReference(type));
        }
    }

    private final class ServiceFunction<T> implements Function<ServiceReference, T>
    {
        @Override
        @SuppressWarnings("unchecked")
        public T apply(ServiceReference reference)
        {
            return (T) bundleContext.getService(reference);
        }
    }
}
