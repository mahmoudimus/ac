package com.atlassian.plugin.remotable.sisu;

import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.module.ModuleFactory;
import com.atlassian.plugin.osgi.bridge.external.PluginRetrievalService;
import com.atlassian.plugin.remotable.api.annotation.PublicComponent;
import com.google.inject.AbstractModule;
import com.google.inject.Provider;
import com.google.inject.TypeLiteral;
import com.google.inject.matcher.Matchers;
import com.google.inject.spi.InjectionListener;
import com.google.inject.spi.TypeEncounter;
import com.google.inject.spi.TypeListener;
import org.ops4j.peaberry.Peaberry;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceFactory;
import org.osgi.framework.ServiceRegistration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.concurrent.Callable;

import static com.google.common.base.Preconditions.checkNotNull;
import static org.ops4j.peaberry.Peaberry.osgiModule;

public final class RemotableModule extends AbstractModule
{
    private final Bundle bundle;

    public RemotableModule(Bundle bundle)
    {
        this.bundle = checkNotNull(bundle);
    }

    @Override
    protected void configure()
    {
        final BundleContext bundleContext = bundle.getBundleContext();

        install(osgiModule(bundleContext));

        bind(ModuleFactory.class).toInstance(
                (ModuleFactory)
                        bundleContext.getService(bundleContext.getServiceReference(ModuleFactory.class.getName()))
        );

        bind(Plugin.class).toInstance(
                ((PluginRetrievalService)
                        bundleContext.getService(
                                bundleContext.getServiceReference(PluginRetrievalService.class.getName()))
                ).getPlugin());

        bind(PluginEventIntegration.class).asEagerSingleton();

        registerPostConstructHandler();
        registerPreDestroyHandler();
        registerPublicComponentHandler();
    }

    private void registerPublicComponentHandler()
    {
        bindListener(Matchers.any(), new TypeListener()
        {
            @Override
            public <I> void hear(TypeLiteral<I> type, final TypeEncounter<I> encounter)
            {
                final Class<?> clazz = type.getRawType();
                final PublicComponent publicComponent = clazz.getAnnotation(PublicComponent.class);
                if (publicComponent != null)
                {
                    String[] infs = new String[publicComponent.value().length];
                    for (int x = 0; x < publicComponent.value().length; x++)
                    {
                        infs[x] = publicComponent.value()[x].getName();
                    }

                    final Provider<?> provider = encounter.getProvider(clazz);
                    bundle.getBundleContext().registerService(infs, new ServiceFactory()
                    {
                        @Override
                        public Object getService(Bundle bundle, ServiceRegistration registration)
                        {
                            Object service = provider.get();
                            if (service instanceof ServiceFactory)
                            {
                                ServiceFactory serviceFactory = (ServiceFactory) wrapService(new Class[]{ServiceFactory.class}, service);
                                return wrapService(publicComponent.value(), serviceFactory.getService(bundle, registration));
                            }
                            else
                            {
                                return wrapService(publicComponent.value(), service);
                            }
                        }

                        @Override
                        public void ungetService(Bundle bundle, ServiceRegistration registration, Object service)
                        {
                            Object provided = provider.get();
                            if (provided instanceof ServiceFactory)
                            {
                                ((ServiceFactory) provided).ungetService(bundle, registration, service);
                            }
                        }
                    }, null);
                }
            }
        });
    }

    private void registerPreDestroyHandler()
    {
        final Disposer disposer = new BundleDisposer(bundle);

        bindListener(Matchers.any(), new AbstractMethodTypeListener(PreDestroy.class)
        {
            @Override
            protected <I> void hear(final Method method, TypeEncounter<I> encounter)
            {
                encounter.register(new InjectionListener<I>()
                {
                    public void afterInjection(I injectee)
                    {
                        disposer.register(method, injectee);
                    }
                });
            }
        });
    }

    private void registerPostConstructHandler()
    {
        bindListener(Matchers.any(), new AbstractMethodTypeListener(PostConstruct.class)
        {
            @Override
            protected <I> void hear(final Method method, TypeEncounter<I> encounter)
            {
                encounter.register(new InjectionListener<I>()
                {
                    public void afterInjection(I injectee)
                    {
                        try
                        {
                            method.invoke(injectee);
                        }
                        catch (IllegalAccessException e)
                        {
                            throw new RuntimeException(e);
                        }
                        catch (InvocationTargetException e)
                        {
                            throw new RuntimeException(e);
                        }
                        catch (Exception e)
                        {
                            throw new RuntimeException(e);
                        }
                    }
                });
            }
        });
    }

    /**
     * Wraps the service in a dynamic proxy that ensures all methods are executed with the object class's class loader
     * as the context class loader
     *
     * @param interfaces The interfaces to proxy
     * @param service    The instance to proxy
     * @return A proxy that wraps the service
     */
    protected Object wrapService(final Class<?>[] interfaces, final Object service)
    {
        return Proxy.newProxyInstance(service.getClass().getClassLoader(), interfaces,
                new ContextClassLoaderSettingInvocationHandler(service));
    }

    /**
     * InvocationHandler for a dynamic proxy that ensures all methods are executed with the object
     * class's class loader as the context class loader.
     */
    private static class ContextClassLoaderSettingInvocationHandler implements InvocationHandler
    {
        private final Object service;

        ContextClassLoaderSettingInvocationHandler(final Object service)
        {
            this.service = service;
        }

        public Object invoke(final Object o, final Method method, final Object[] objects) throws
                Throwable
        {
            final Thread thread = Thread.currentThread();
            final ClassLoader ccl = thread.getContextClassLoader();
            try
            {
                thread.setContextClassLoader(service.getClass().getClassLoader());
                return method.invoke(service, objects);
            }
            catch (final InvocationTargetException e)
            {
                throw e.getTargetException();
            }
            finally
            {
                thread.setContextClassLoader(ccl);
            }
        }
    }
}
