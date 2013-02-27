package com.atlassian.plugin.remotable.sisu;

import com.atlassian.plugin.remotable.api.annotation.PublicComponent;
import com.google.inject.Provider;
import com.google.inject.TypeLiteral;
import com.google.inject.spi.TypeEncounter;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceFactory;
import org.osgi.framework.ServiceRegistration;

import java.lang.reflect.Proxy;
import java.util.Arrays;

import static com.google.common.base.Preconditions.checkNotNull;

final class PublicComponentAnnotatedClassTypeListener extends AbstractAnnotatedClassTypeListener<PublicComponent>
{
    private final BundleContext bundleContext;

    PublicComponentAnnotatedClassTypeListener(BundleContext bundleContext)
    {
        super(PublicComponent.class);
        this.bundleContext = checkNotNull(bundleContext);
    }

    @Override
    public <I> void hear(TypeLiteral<I> type, final TypeEncounter<I> encounter, final PublicComponent annotation)
    {
        final String[] interfaces = new String[annotation.value().length];
        for (int x = 0; x < annotation.value().length; x++)
        {
            interfaces[x] = annotation.value()[x].getName();
        }

        final Class<? super I> clazz = type.getRawType();
        final Provider<?> provider = encounter.getProvider(clazz);

        final String interfacesAsString = Arrays.toString(interfaces);
        logger.debug("Registering service (public component) implementation {} as {}", clazz, interfacesAsString);

        bundleContext.registerService(interfaces, new ServiceFactory()
        {
            @Override
            public Object getService(Bundle bundle, ServiceRegistration registration)
            {
                logger.debug("Bundle {} is getting a service reference for {} exposed as {}", new Object[]{bundle, clazz, interfacesAsString});
                Object service = provider.get();
                if (service instanceof ServiceFactory)
                {
                    ServiceFactory serviceFactory = (ServiceFactory) wrapService(new Class[]{ServiceFactory.class}, service);
                    return wrapService(annotation.value(), serviceFactory.getService(bundle, registration));
                }
                else
                {
                    return wrapService(annotation.value(), service);
                }
            }

            @Override
            public void ungetService(Bundle bundle, ServiceRegistration registration, Object service)
            {
                logger.debug("Bundle {} is un-getting a service reference for {} exposed as {}", new Object[]{bundle, clazz, interfacesAsString});
                Object provided = provider.get();
                if (provided instanceof ServiceFactory)
                {
                    ((ServiceFactory) provided).ungetService(bundle, registration, service);
                }
            }
        }, null);
    }

    /**
     * Wraps the service in a dynamic proxy that ensures all methods are executed with the object class's class loader
     * as the context class loader
     *
     * @param interfaces The interfaces to proxy
     * @param service The instance to proxy
     * @return A proxy that wraps the service
     */
    private Object wrapService(final Class<?>[] interfaces, final Object service)
    {
        return Proxy.newProxyInstance(
                service.getClass().getClassLoader(),
                interfaces,
                new ContextClassLoaderSettingInvocationHandler(service));
    }
}
